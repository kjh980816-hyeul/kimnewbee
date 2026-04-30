import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { fetchClips, fetchClip, createClip, toggleClipLike } from '@/api/clip';
import { clipFixtures } from '@/mocks/data/clips';
import type { Clip, ClipSource } from '@/types/clip';

const API_URL = 'http://localhost:8080';

let store: Clip[] = [];

function detectSource(url: string): ClipSource {
  if (/youtube\.com|youtu\.be/.test(url)) return 'youtube';
  if (/chzzk\.naver\.com/.test(url)) return 'chzzk';
  return 'other';
}

const server = setupServer(
  http.get(`${API_URL}/api/clips`, () => {
    const list = store.map((c) => ({
      id: c.id,
      title: c.title,
      author: c.author,
      videoUrl: c.videoUrl,
      source: c.source,
      createdAt: c.createdAt,
      likeCount: c.likeCount,
      commentCount: c.commentCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),
  http.get(`${API_URL}/api/clips/:id`, ({ params }) => {
    const id = Number(params['id']);
    const c = store.find((x) => x.id === id);
    return c ? HttpResponse.json(c) : HttpResponse.json({}, { status: 404 });
  }),
  http.post(`${API_URL}/api/clips`, async ({ request }) => {
    const body = (await request.json()) as { title: string; videoUrl: string; description: string };
    const now = new Date().toISOString();
    const c: Clip = {
      id: 9999,
      title: body.title,
      videoUrl: body.videoUrl,
      source: detectSource(body.videoUrl),
      description: body.description,
      author: '초록고추',
      createdAt: now,
      updatedAt: now,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      likedByMe: false,
    };
    store.unshift(c);
    return HttpResponse.json(c, { status: 201 });
  }),
  http.post(`${API_URL}/api/clips/:id/like`, ({ params }) => {
    const id = Number(params['id']);
    const c = store.find((x) => x.id === id);
    if (!c) return HttpResponse.json({}, { status: 404 });
    c.likedByMe = !c.likedByMe;
    c.likeCount += c.likedByMe ? 1 : -1;
    return HttpResponse.json({ liked: c.likedByMe, likeCount: c.likeCount });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  store = clipFixtures.map((c) => ({ ...c }));
});

describe('clip api', () => {
  it('list response includes source tag', async () => {
    const res = await fetchClips();
    expect(res.data.every((c) => ['youtube', 'chzzk', 'other'].includes(c.source))).toBe(true);
  });

  it('detail includes description and likedByMe', async () => {
    const fixture = clipFixtures[0];
    if (!fixture) throw new Error('clipFixtures empty');
    const c = await fetchClip(fixture.id);
    expect(c.description).toBeTruthy();
    expect(c.likedByMe).toBe(false);
  });

  it('createClip detects youtube source from URL', async () => {
    const c = await createClip({
      title: 'yt',
      videoUrl: 'https://www.youtube.com/watch?v=abc123',
      description: '',
    });
    expect(c.source).toBe('youtube');
  });

  it('createClip detects chzzk source from URL', async () => {
    const c = await createClip({
      title: 'chzzk',
      videoUrl: 'https://chzzk.naver.com/clips/xyz789',
      description: '',
    });
    expect(c.source).toBe('chzzk');
  });

  it('createClip falls back to other for unknown host', async () => {
    const c = await createClip({
      title: 'misc',
      videoUrl: 'https://vimeo.com/12345',
      description: '',
    });
    expect(c.source).toBe('other');
  });

  it('toggle like flips state and adjusts count', async () => {
    const fixture = clipFixtures[0];
    if (!fixture) throw new Error('clipFixtures empty');
    const initial = fixture.likeCount;
    const r1 = await toggleClipLike(fixture.id);
    expect(r1.liked).toBe(true);
    expect(r1.likeCount).toBe(initial + 1);
    const r2 = await toggleClipLike(fixture.id);
    expect(r2.liked).toBe(false);
    expect(r2.likeCount).toBe(initial);
  });
});
