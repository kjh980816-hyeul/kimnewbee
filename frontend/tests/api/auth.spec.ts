import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { loginWithNaver } from '@/api/auth';

const API_URL = 'http://localhost:8080';

const server = setupServer(
  http.post(`${API_URL}/api/auth/naver`, () => {
    return HttpResponse.json({
      user: {
        id: 11234,
        nickname: '초록고추1234',
        tier: 'pepper',
        profileImage: null,
        createdAt: new Date().toISOString(),
      },
    });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());

describe('auth api', () => {
  it('loginWithNaver returns user with pepper tier and 초록고추 nickname', async () => {
    const res = await loginWithNaver();
    expect(res.user.tier).toBe('pepper');
    expect(res.user.nickname).toMatch(/^초록고추/);
    expect(res.user.profileImage).toBeNull();
    expect(res.user.id).toBeGreaterThan(0);
  });
});
