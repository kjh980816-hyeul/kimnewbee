import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { fetchNotices, fetchNotice } from '@/api/notice';
import { noticeFixtures } from '@/mocks/data/notices';

const API_URL = 'http://localhost:8080';

const server = setupServer(
  http.get(`${API_URL}/api/notices`, () => {
    const list = noticeFixtures.map((n) => ({
      id: n.id,
      title: n.title,
      author: n.author,
      createdAt: n.createdAt,
      viewCount: n.viewCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),
  http.get(`${API_URL}/api/notices/:id`, ({ params }) => {
    const id = Number(params['id']);
    const notice = noticeFixtures.find((n) => n.id === id);
    return notice
      ? HttpResponse.json(notice)
      : HttpResponse.json({ message: 'Not found' }, { status: 404 });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());

describe('notice api', () => {
  it('returns full list with total count', async () => {
    const res = await fetchNotices();
    expect(res.total).toBe(noticeFixtures.length);
    expect(res.data).toHaveLength(noticeFixtures.length);
    expect(res.data[0]).toMatchObject({
      id: expect.any(Number),
      title: expect.any(String),
      author: expect.any(String),
      createdAt: expect.any(String),
      viewCount: expect.any(Number),
    });
  });

  it('list items do not include content (lighter payload for list)', async () => {
    const res = await fetchNotices();
    expect(res.data[0]).not.toHaveProperty('content');
  });

  it('fetches single notice with full content', async () => {
    const fixture = noticeFixtures[0];
    if (!fixture) throw new Error('noticeFixtures must have at least one entry');
    const notice = await fetchNotice(fixture.id);
    expect(notice).toEqual(fixture);
    expect(notice.content).toBeTruthy();
  });

  it('throws on nonexistent notice id', async () => {
    await expect(fetchNotice(99999)).rejects.toThrow();
  });
});
