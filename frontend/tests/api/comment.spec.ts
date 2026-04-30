import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { fetchComments, createComment, deleteComment } from '@/api/comment';
import { commentFixtures } from '@/mocks/data/comments';
import type { Comment } from '@/types/comment';

const API_URL = 'http://localhost:8080';

let store: Comment[] = [];
let nextId = 1000;

const server = setupServer(
  http.get(`${API_URL}/api/posts/:postId/comments`, ({ params }) => {
    const postId = Number(params['postId']);
    return HttpResponse.json(store.filter((c) => c.postId === postId));
  }),
  http.post(`${API_URL}/api/posts/:postId/comments`, async ({ params, request }) => {
    const postId = Number(params['postId']);
    const body = (await request.json()) as { parentId: number | null; content: string };
    const c: Comment = {
      id: nextId++,
      postId,
      parentId: body.parentId,
      author: '초록고추',
      content: body.content,
      createdAt: new Date().toISOString(),
      likeCount: 0,
      deleted: false,
    };
    store.push(c);
    return HttpResponse.json(c, { status: 201 });
  }),
  http.delete(`${API_URL}/api/comments/:id`, ({ params }) => {
    const id = Number(params['id']);
    const c = store.find((x) => x.id === id);
    if (!c) return HttpResponse.json({}, { status: 404 });
    c.deleted = true;
    c.content = '삭제된 댓글이에요';
    return new HttpResponse(null, { status: 204 });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  store = commentFixtures.map((c) => ({ ...c }));
  nextId = 1000;
});

describe('comment api', () => {
  it('fetches comments scoped to a post', async () => {
    const comments = await fetchComments(101);
    expect(comments.every((c) => c.postId === 101)).toBe(true);
    expect(comments.length).toBeGreaterThan(0);
  });

  it('returns empty list for post with no comments', async () => {
    const comments = await fetchComments(99999);
    expect(comments).toEqual([]);
  });

  it('creates a top-level comment (parentId null)', async () => {
    const c = await createComment({ postId: 101, parentId: null, content: '안녕' });
    expect(c.parentId).toBeNull();
    expect(c.content).toBe('안녕');
    const after = await fetchComments(101);
    expect(after.find((x) => x.id === c.id)).toBeDefined();
  });

  it('creates a reply with parentId set', async () => {
    const reply = await createComment({ postId: 101, parentId: 1, content: '답글' });
    expect(reply.parentId).toBe(1);
  });

  it('marks comment as deleted (soft delete) on delete', async () => {
    await deleteComment(1);
    const after = await fetchComments(101);
    const target = after.find((c) => c.id === 1);
    expect(target?.deleted).toBe(true);
    expect(target?.content).toBe('삭제된 댓글이에요');
  });
});
