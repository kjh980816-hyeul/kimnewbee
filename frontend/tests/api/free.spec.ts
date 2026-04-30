import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import {
  fetchFreePosts,
  fetchFreePost,
  createFreePost,
  toggleFreePostLike,
} from '@/api/free';
import { freePostFixtures } from '@/mocks/data/free';
import type { FreePost } from '@/types/free';

const API_URL = 'http://localhost:8080';

let store: FreePost[] = [];
let likedSet: Set<number> = new Set();

const server = setupServer(
  http.get(`${API_URL}/api/free`, () => {
    const list = store.map((p) => ({
      id: p.id,
      title: p.title,
      author: p.author,
      createdAt: p.createdAt,
      viewCount: p.viewCount,
      likeCount: p.likeCount,
      commentCount: p.commentCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),
  http.get(`${API_URL}/api/free/:id`, ({ params }) => {
    const id = Number(params['id']);
    const post = store.find((p) => p.id === id);
    return post ? HttpResponse.json(post) : HttpResponse.json({}, { status: 404 });
  }),
  http.post(`${API_URL}/api/free`, async ({ request }) => {
    const body = (await request.json()) as { title: string; content: string };
    const now = new Date().toISOString();
    const newPost: FreePost = {
      id: 9999,
      title: body.title,
      content: body.content,
      author: '초록고추',
      createdAt: now,
      updatedAt: now,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      likedByMe: false,
    };
    store.unshift(newPost);
    return HttpResponse.json(newPost, { status: 201 });
  }),
  http.post(`${API_URL}/api/free/:id/like`, ({ params }) => {
    const id = Number(params['id']);
    const post = store.find((p) => p.id === id);
    if (!post) return HttpResponse.json({}, { status: 404 });
    if (likedSet.has(id)) {
      likedSet.delete(id);
      post.likeCount -= 1;
      post.likedByMe = false;
    } else {
      likedSet.add(id);
      post.likeCount += 1;
      post.likedByMe = true;
    }
    return HttpResponse.json({ liked: post.likedByMe, likeCount: post.likeCount });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  store = freePostFixtures.map((p) => ({ ...p }));
  likedSet = new Set();
});

describe('free post api', () => {
  it('fetches list with summary fields only', async () => {
    const res = await fetchFreePosts();
    expect(res.total).toBe(freePostFixtures.length);
    expect(res.data[0]).not.toHaveProperty('content');
    expect(res.data[0]).toMatchObject({
      id: expect.any(Number),
      likeCount: expect.any(Number),
      commentCount: expect.any(Number),
    });
  });

  it('fetches detail with content + likedByMe flag', async () => {
    const fixture = freePostFixtures[0];
    if (!fixture) throw new Error('freePostFixtures is empty');
    const post = await fetchFreePost(fixture.id);
    expect(post.content).toBeTruthy();
    expect(post.likedByMe).toBe(false);
  });

  it('creates new post with submitted title/content', async () => {
    const created = await createFreePost({ title: '테스트', content: '본문' });
    expect(created.id).toBe(9999);
    expect(created.title).toBe('테스트');
    expect(created.content).toBe('본문');
    expect(created.author).toBe('초록고추');
  });

  it('toggles like state and increments/decrements count', async () => {
    const fixture = freePostFixtures[0];
    if (!fixture) throw new Error('freePostFixtures is empty');
    const initialCount = fixture.likeCount;

    const r1 = await toggleFreePostLike(fixture.id);
    expect(r1.liked).toBe(true);
    expect(r1.likeCount).toBe(initialCount + 1);

    const r2 = await toggleFreePostLike(fixture.id);
    expect(r2.liked).toBe(false);
    expect(r2.likeCount).toBe(initialCount);
  });
});
