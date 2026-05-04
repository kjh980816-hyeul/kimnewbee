import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import {
  fetchFanartList,
  fetchFanart,
  createFanart,
  updateFanart,
  toggleFanartLike,
} from '@/api/fanart';
import { fanartFixtures } from '@/mocks/data/fanart';
import type { Fanart } from '@/types/fanart';

const API_URL = 'http://localhost:8080';

let store: Fanart[] = [];

const server = setupServer(
  http.get(`${API_URL}/api/fanart`, () => {
    const list = store.map((f) => ({
      id: f.id,
      title: f.title,
      author: f.author,
      thumbnailUrl: f.thumbnailUrl,
      createdAt: f.createdAt,
      likeCount: f.likeCount,
      commentCount: f.commentCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),
  http.get(`${API_URL}/api/fanart/:id`, ({ params }) => {
    const id = Number(params['id']);
    const f = store.find((x) => x.id === id);
    return f ? HttpResponse.json(f) : HttpResponse.json({}, { status: 404 });
  }),
  http.post(`${API_URL}/api/fanart`, async ({ request }) => {
    const body = (await request.json()) as { title: string; imageUrl: string; content: string };
    const now = new Date().toISOString();
    const newFanart: Fanart = {
      id: 9999,
      title: body.title,
      imageUrl: body.imageUrl,
      thumbnailUrl: body.imageUrl,
      content: body.content,
      author: '초록고추',
      createdAt: now,
      updatedAt: now,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      likedByMe: false,
    };
    store.unshift(newFanart);
    return HttpResponse.json(newFanart, { status: 201 });
  }),
  http.patch(`${API_URL}/api/fanart/:id`, async ({ params, request }) => {
    const id = Number(params['id']);
    const f = store.find((x) => x.id === id);
    if (!f) return HttpResponse.json({}, { status: 404 });
    const body = (await request.json()) as { title: string; imageUrl: string; content: string };
    f.title = body.title;
    f.imageUrl = body.imageUrl;
    f.thumbnailUrl = body.imageUrl;
    f.content = body.content;
    f.updatedAt = new Date().toISOString();
    return HttpResponse.json(f);
  }),
  http.post(`${API_URL}/api/fanart/:id/like`, ({ params }) => {
    const id = Number(params['id']);
    const f = store.find((x) => x.id === id);
    if (!f) return HttpResponse.json({}, { status: 404 });
    f.likedByMe = !f.likedByMe;
    f.likeCount += f.likedByMe ? 1 : -1;
    return HttpResponse.json({ liked: f.likedByMe, likeCount: f.likeCount });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  store = fanartFixtures.map((f) => ({ ...f }));
});

describe('fanart api', () => {
  it('fetches list with thumbnail (no full imageUrl/content)', async () => {
    const res = await fetchFanartList();
    expect(res.total).toBe(fanartFixtures.length);
    expect(res.data[0]).toHaveProperty('thumbnailUrl');
    expect(res.data[0]).not.toHaveProperty('imageUrl');
    expect(res.data[0]).not.toHaveProperty('content');
  });

  it('fetches detail with full imageUrl + content + viewCount', async () => {
    const fixture = fanartFixtures[0];
    if (!fixture) throw new Error('fanartFixtures is empty');
    const f = await fetchFanart(fixture.id);
    expect(f.imageUrl).toBeTruthy();
    expect(f.viewCount).toBeGreaterThanOrEqual(0);
  });

  it('creates fanart with imageUrl carried over to thumbnail', async () => {
    const created = await createFanart({
      title: '테스트',
      imageUrl: 'https://example.com/img.png',
      content: '설명',
    });
    expect(created.imageUrl).toBe('https://example.com/img.png');
    expect(created.thumbnailUrl).toBe('https://example.com/img.png');
  });

  it('updates fanart title/imageUrl/content (thumbnail follows)', async () => {
    const fixture = fanartFixtures[0];
    if (!fixture) throw new Error('fanartFixtures is empty');
    const updated = await updateFanart(fixture.id, {
      title: '수정',
      imageUrl: 'https://new.example/img.png',
      content: '수정 설명',
    });
    expect(updated.title).toBe('수정');
    expect(updated.imageUrl).toBe('https://new.example/img.png');
    expect(updated.thumbnailUrl).toBe('https://new.example/img.png');
  });

  it('toggles fanart like and updates count', async () => {
    const fixture = fanartFixtures[0];
    if (!fixture) throw new Error('fanartFixtures is empty');
    const initial = fixture.likeCount;

    const r1 = await toggleFanartLike(fixture.id);
    expect(r1.liked).toBe(true);
    expect(r1.likeCount).toBe(initial + 1);

    const r2 = await toggleFanartLike(fixture.id);
    expect(r2.liked).toBe(false);
    expect(r2.likeCount).toBe(initial);
  });
});
