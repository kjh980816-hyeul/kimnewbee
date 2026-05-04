import { http, HttpResponse } from 'msw';
import { adminUserFixtures } from '../data/adminUsers';
import { readMockUser } from './auth';
import type { AdminUser, ChangeTierInput, AdjustPointsInput } from '@/types/admin';
import type { Tier } from '@/types/offline';

const API_URL = import.meta.env.VITE_API_URL;

const adminUserStore: AdminUser[] = adminUserFixtures.map((u) => ({ ...u }));

function isAdmin(): boolean {
  try {
    return globalThis.localStorage?.getItem('mock-is-admin') === 'true';
  } catch {
    return false;
  }
}

function deny(status: number, message: string) {
  return HttpResponse.json({ message }, { status });
}

function gateOwner() {
  const user = readMockUser();
  if (!user) return deny(401, '로그인이 필요해요');
  if (!isAdmin()) return deny(403, '관리자 권한이 필요해요');
  return null;
}

export const adminHandlers = [
  http.get(`${API_URL}/api/admin/dashboard`, () => {
    const blocked = gateOwner();
    if (blocked) return blocked;
    return HttpResponse.json({
      totalUsers: adminUserStore.length,
      totalPosts: 47,
      totalComments: 312,
      totalLikes: 1284,
    });
  }),

  http.get(`${API_URL}/api/admin/users`, () => {
    const blocked = gateOwner();
    if (blocked) return blocked;
    return HttpResponse.json({ data: adminUserStore, total: adminUserStore.length });
  }),

  http.patch(`${API_URL}/api/admin/users/:id/tier`, async ({ params, request }) => {
    const blocked = gateOwner();
    if (blocked) return blocked;
    const id = Number(params['id']);
    const target = adminUserStore.find((u) => u.id === id);
    if (!target) return deny(404, '회원을 찾을 수 없어요');
    const body = (await request.json()) as ChangeTierInput;
    target.tier = body.tier.toLowerCase() as Tier;
    return HttpResponse.json(target);
  }),

  http.patch(`${API_URL}/api/admin/users/:id/points`, async ({ params, request }) => {
    const blocked = gateOwner();
    if (blocked) return blocked;
    const id = Number(params['id']);
    const target = adminUserStore.find((u) => u.id === id);
    if (!target) return deny(404, '회원을 찾을 수 없어요');
    const body = (await request.json()) as AdjustPointsInput;
    target.points = Math.max(0, target.points + body.delta);
    return HttpResponse.json(target);
  }),
];
