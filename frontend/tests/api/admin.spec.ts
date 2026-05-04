import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import {
  fetchAdminDashboard,
  fetchAdminUsers,
  changeUserTier,
  adjustUserPoints,
} from '@/api/admin';
import { adminUserFixtures } from '@/mocks/data/adminUsers';
import type { AdminUser, ChangeTierInput, AdjustPointsInput } from '@/types/admin';
import type { Tier } from '@/types/offline';

const API_URL = 'http://localhost:8080';

let store: AdminUser[] = [];
let viewerIsAdmin = true;

const server = setupServer(
  http.get(`${API_URL}/api/admin/dashboard`, () => {
    if (!viewerIsAdmin) return HttpResponse.json({}, { status: 403 });
    return HttpResponse.json({
      totalUsers: store.length,
      totalPosts: 47,
      totalComments: 312,
      totalLikes: 1284,
    });
  }),
  http.get(`${API_URL}/api/admin/users`, () => {
    if (!viewerIsAdmin) return HttpResponse.json({}, { status: 403 });
    return HttpResponse.json({ data: store, total: store.length });
  }),
  http.patch(`${API_URL}/api/admin/users/:id/tier`, async ({ params, request }) => {
    if (!viewerIsAdmin) return HttpResponse.json({}, { status: 403 });
    const id = Number(params['id']);
    const target = store.find((u) => u.id === id);
    if (!target) return HttpResponse.json({}, { status: 404 });
    const body = (await request.json()) as ChangeTierInput;
    target.tier = body.tier.toLowerCase() as Tier;
    return HttpResponse.json(target);
  }),
  http.patch(`${API_URL}/api/admin/users/:id/points`, async ({ params, request }) => {
    if (!viewerIsAdmin) return HttpResponse.json({}, { status: 403 });
    const id = Number(params['id']);
    const target = store.find((u) => u.id === id);
    if (!target) return HttpResponse.json({}, { status: 404 });
    const body = (await request.json()) as AdjustPointsInput;
    target.points = Math.max(0, target.points + body.delta);
    return HttpResponse.json(target);
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  store = adminUserFixtures.map((u) => ({ ...u }));
  viewerIsAdmin = true;
});

describe('admin api', () => {
  it('fetches dashboard with 4 counts', async () => {
    const dash = await fetchAdminDashboard();
    expect(dash.totalUsers).toBe(adminUserFixtures.length);
    expect(dash.totalPosts).toBeGreaterThan(0);
    expect(dash.totalLikes).toBeGreaterThan(0);
  });

  it('non-admin viewer gets 403 on dashboard', async () => {
    viewerIsAdmin = false;
    await expect(fetchAdminDashboard()).rejects.toThrow();
  });

  it('fetches user list with all fixture users', async () => {
    const res = await fetchAdminUsers();
    expect(res.total).toBe(adminUserFixtures.length);
    expect(res.data[0]).toHaveProperty('naverId');
  });

  it('changes user tier and reflects in store', async () => {
    const target = adminUserFixtures.find((u) => u.tier === 'pepper');
    if (!target) throw new Error('no pepper fixture');
    const updated = await changeUserTier(target.id, { tier: 'CORN' });
    expect(updated.tier).toBe('corn');
  });

  it('adjusts user points (positive delta)', async () => {
    const target = adminUserFixtures.find((u) => u.tier === 'seed');
    if (!target) throw new Error('no seed fixture');
    const updated = await adjustUserPoints(target.id, { delta: 50 });
    expect(updated.points).toBe(target.points + 50);
  });

  it('adjusts user points floor 0 on negative overflow', async () => {
    const target = adminUserFixtures.find((u) => u.tier === 'seed');
    if (!target) throw new Error('no seed fixture');
    const updated = await adjustUserPoints(target.id, { delta: -9999 });
    expect(updated.points).toBe(0);
  });

  it('returns 404 for unknown user id', async () => {
    await expect(adjustUserPoints(99999, { delta: 1 })).rejects.toThrow();
  });
});
