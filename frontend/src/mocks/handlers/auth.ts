import { http, HttpResponse } from 'msw';
import type { CurrentUser } from '@/types/user';

const API_URL = import.meta.env.VITE_API_URL;

function generateMockUser(): CurrentUser {
  const suffix = Math.floor(Math.random() * 9000) + 1000;
  return {
    id: 10000 + suffix,
    nickname: `초록고추${suffix}`,
    tier: 'pepper',
    profileImage: null,
    createdAt: new Date().toISOString(),
  };
}

export const authHandlers = [
  http.post(`${API_URL}/api/auth/naver`, () => {
    return HttpResponse.json({ user: generateMockUser() });
  }),
];
