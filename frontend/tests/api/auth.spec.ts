import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { loginWithNaver, logout } from '@/api/auth';

const API_URL = 'http://localhost:8080';

let logoutCalled = false;

const server = setupServer(
  http.post(`${API_URL}/api/auth/naver`, () => {
    return HttpResponse.json({
      user: {
        id: 11234,
        nickname: '초록고추1234',
        tier: 'pepper',
        points: 0,
        profileImage: null,
        createdAt: new Date().toISOString(),
      },
    });
  }),
  http.post(`${API_URL}/api/auth/logout`, () => {
    logoutCalled = true;
    return new HttpResponse(null, { status: 204 });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());

describe('auth api', () => {
  it('loginWithNaver returns user with pepper tier and points 0', async () => {
    const res = await loginWithNaver();
    expect(res.user.tier).toBe('pepper');
    expect(res.user.points).toBe(0);
    expect(res.user.nickname).toMatch(/^초록고추/);
  });

  it('logout calls POST /api/auth/logout', async () => {
    logoutCalled = false;
    await logout();
    expect(logoutCalled).toBe(true);
  });
});
