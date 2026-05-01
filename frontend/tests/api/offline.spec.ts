import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import {
  fetchOfflineReviews,
  fetchOfflineReview,
  createOfflineReview,
  toggleOfflineReviewLike,
} from '@/api/offline';
import { offlineReviewFixtures } from '@/mocks/data/offline';
import type { OfflineReview, Tier } from '@/types/offline';

const API_URL = 'http://localhost:8080';

const TIER_RANK: Record<Tier, number> = {
  seed: 0,
  pepper: 1,
  corn: 2,
  owner: 3,
};

let store: OfflineReview[] = [];
let viewerTier: Tier = 'pepper';

function hasAccess(): boolean {
  return TIER_RANK[viewerTier] >= TIER_RANK.corn;
}

const server = setupServer(
  http.get(`${API_URL}/api/offline`, () => {
    if (!hasAccess()) return HttpResponse.json({}, { status: 403 });
    const list = store.map((r) => ({
      id: r.id,
      title: r.title,
      location: r.location,
      meetupDate: r.meetupDate,
      thumbnailUrl: r.thumbnailUrl,
      preview: r.preview,
      author: r.author,
      createdAt: r.createdAt,
      likeCount: r.likeCount,
      commentCount: r.commentCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),
  http.get(`${API_URL}/api/offline/:id`, ({ params }) => {
    if (!hasAccess()) return HttpResponse.json({}, { status: 403 });
    const id = Number(params['id']);
    const r = store.find((x) => x.id === id);
    return r ? HttpResponse.json(r) : HttpResponse.json({}, { status: 404 });
  }),
  http.post(`${API_URL}/api/offline`, async ({ request }) => {
    if (!hasAccess()) return HttpResponse.json({}, { status: 403 });
    const body = (await request.json()) as {
      title: string;
      location: string;
      meetupDate: string;
      imageUrl: string;
      content: string;
    };
    const now = new Date().toISOString();
    const r: OfflineReview = {
      id: 9999,
      title: body.title,
      location: body.location,
      meetupDate: body.meetupDate,
      imageUrl: body.imageUrl,
      thumbnailUrl: body.imageUrl,
      preview: body.content.slice(0, 80),
      content: body.content,
      author: '초록고추',
      createdAt: now,
      updatedAt: now,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      likedByMe: false,
    };
    store.unshift(r);
    return HttpResponse.json(r, { status: 201 });
  }),
  http.post(`${API_URL}/api/offline/:id/like`, ({ params }) => {
    if (!hasAccess()) return HttpResponse.json({}, { status: 403 });
    const id = Number(params['id']);
    const r = store.find((x) => x.id === id);
    if (!r) return HttpResponse.json({}, { status: 404 });
    r.likedByMe = !r.likedByMe;
    r.likeCount += r.likedByMe ? 1 : -1;
    return HttpResponse.json({ liked: r.likedByMe, likeCount: r.likeCount });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  store = offlineReviewFixtures.map((r) => ({ ...r }));
  viewerTier = 'pepper';
});

describe('offline review api', () => {
  it('pepper tier (below corn) gets 403 on list', async () => {
    viewerTier = 'pepper';
    await expect(fetchOfflineReviews()).rejects.toThrow();
  });

  it('seed tier gets 403 too', async () => {
    viewerTier = 'seed';
    await expect(fetchOfflineReviews()).rejects.toThrow();
  });

  it('corn tier can read list', async () => {
    viewerTier = 'corn';
    const res = await fetchOfflineReviews();
    expect(res.total).toBe(offlineReviewFixtures.length);
  });

  it('owner tier can read list', async () => {
    viewerTier = 'owner';
    const res = await fetchOfflineReviews();
    expect(res.data.length).toBeGreaterThan(0);
  });

  it('corn tier can read detail with content', async () => {
    viewerTier = 'corn';
    const fixture = offlineReviewFixtures[0];
    if (!fixture) throw new Error('fixtures empty');
    const r = await fetchOfflineReview(fixture.id);
    expect(r.content).toBeTruthy();
    expect(r.location).toBe(fixture.location);
  });

  it('pepper tier blocked from creating', async () => {
    viewerTier = 'pepper';
    await expect(
      createOfflineReview({
        title: '시도',
        location: '어디',
        meetupDate: '2026-05-01',
        imageUrl: 'https://example.com/img.png',
        content: '본문',
      }),
    ).rejects.toThrow();
  });

  it('corn tier toggle like updates count', async () => {
    viewerTier = 'corn';
    const fixture = offlineReviewFixtures[0];
    if (!fixture) throw new Error('fixtures empty');
    const initial = fixture.likeCount;
    const r1 = await toggleOfflineReviewLike(fixture.id);
    expect(r1.liked).toBe(true);
    expect(r1.likeCount).toBe(initial + 1);
  });
});
