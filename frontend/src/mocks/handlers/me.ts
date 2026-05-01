import { http, HttpResponse } from 'msw';
import { readMockUser } from './auth';
import type { UserStats } from '@/types/user';

const API_URL = import.meta.env.VITE_API_URL;

export const meHandlers = [
  http.get(`${API_URL}/api/me`, () => {
    const user = readMockUser();
    if (!user) {
      return HttpResponse.json({ message: '로그인이 필요해요' }, { status: 401 });
    }
    return HttpResponse.json(user);
  }),

  http.get(`${API_URL}/api/me/stats`, () => {
    const user = readMockUser();
    if (!user) {
      return HttpResponse.json({ message: '로그인이 필요해요' }, { status: 401 });
    }
    const stats: UserStats = {
      postCount: 7,
      commentCount: 23,
      likeGivenCount: 41,
      attendanceStreak: 5,
    };
    return HttpResponse.json(stats);
  }),
];
