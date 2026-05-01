import { http, HttpResponse } from 'msw';
import type { CurrentUser } from '@/types/user';

const API_URL = import.meta.env.VITE_API_URL;
const STORAGE_KEY = 'mock-user';

function readUser(): CurrentUser | null {
  try {
    const raw = globalThis.localStorage?.getItem(STORAGE_KEY);
    if (!raw) return null;
    return JSON.parse(raw) as CurrentUser;
  } catch {
    return null;
  }
}

function writeUser(user: CurrentUser | null): void {
  try {
    if (user) {
      globalThis.localStorage?.setItem(STORAGE_KEY, JSON.stringify(user));
    } else {
      globalThis.localStorage?.removeItem(STORAGE_KEY);
    }
  } catch {
    /* ignore */
  }
}

function generateMockUser(): CurrentUser {
  const suffix = Math.floor(Math.random() * 9000) + 1000;
  return {
    id: 10000 + suffix,
    nickname: `초록고추${suffix}`,
    tier: 'pepper',
    points: 0,
    profileImage: null,
    createdAt: new Date().toISOString(),
  };
}

export const authHandlers = [
  http.post(`${API_URL}/api/auth/naver`, () => {
    let user = readUser();
    if (!user) {
      user = generateMockUser();
      writeUser(user);
    }
    return HttpResponse.json({ user });
  }),

  http.post(`${API_URL}/api/auth/logout`, () => {
    writeUser(null);
    return new HttpResponse(null, { status: 204 });
  }),
];

export { readUser as readMockUser };
