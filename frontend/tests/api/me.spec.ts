import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { fetchMe, fetchMyStats } from '@/api/me';
import type { CurrentUser } from '@/types/user';

const API_URL = 'http://localhost:8080';

let currentUser: CurrentUser | null = null;

const server = setupServer(
  http.get(`${API_URL}/api/me`, () => {
    if (!currentUser) {
      return HttpResponse.json({ message: 'unauthorized' }, { status: 401 });
    }
    return HttpResponse.json(currentUser);
  }),
  http.get(`${API_URL}/api/me/stats`, () => {
    if (!currentUser) {
      return HttpResponse.json({ message: 'unauthorized' }, { status: 401 });
    }
    return HttpResponse.json({
      postCount: 7,
      commentCount: 23,
      likeGivenCount: 41,
      attendanceStreak: 5,
    });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  currentUser = null;
});

describe('me api', () => {
  it('fetchMe returns 401 when not logged in', async () => {
    await expect(fetchMe()).rejects.toThrow();
  });

  it('fetchMe returns current user when logged in', async () => {
    currentUser = {
      id: 12345,
      nickname: '초록고추TEST',
      tier: 'corn',
      points: 1500,
      profileImage: null,
      createdAt: new Date().toISOString(),
    };
    const me = await fetchMe();
    expect(me.id).toBe(12345);
    expect(me.tier).toBe('corn');
    expect(me.points).toBe(1500);
  });

  it('fetchMyStats requires login (401)', async () => {
    await expect(fetchMyStats()).rejects.toThrow();
  });

  it('fetchMyStats returns activity counters when logged in', async () => {
    currentUser = {
      id: 1,
      nickname: 't',
      tier: 'pepper',
      points: 0,
      profileImage: null,
      createdAt: '2026-01-01T00:00:00.000Z',
    };
    const stats = await fetchMyStats();
    expect(stats.postCount).toBeGreaterThanOrEqual(0);
    expect(stats.commentCount).toBeGreaterThanOrEqual(0);
    expect(stats.likeGivenCount).toBeGreaterThanOrEqual(0);
    expect(stats.attendanceStreak).toBeGreaterThanOrEqual(0);
  });
});
