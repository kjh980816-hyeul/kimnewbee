import type { AdminUser } from '@/types/admin';

export const adminUserFixtures: AdminUser[] = [
  {
    id: 1,
    naverId: 'nv-owner-1',
    nickname: '밭주인',
    tier: 'owner',
    points: 9999,
    createdAt: '2025-01-15T09:00:00.000Z',
  },
  {
    id: 2,
    naverId: 'nv-corn-1',
    nickname: '오프참석자',
    tier: 'corn',
    points: 720,
    createdAt: '2025-02-10T11:30:00.000Z',
  },
  {
    id: 3,
    naverId: 'nv-pepper-1',
    nickname: '초록고추1',
    tier: 'pepper',
    points: 145,
    createdAt: '2025-03-22T15:45:00.000Z',
  },
  {
    id: 4,
    naverId: 'nv-pepper-2',
    nickname: '초록고추2',
    tier: 'pepper',
    points: 102,
    createdAt: '2025-04-01T08:15:00.000Z',
  },
  {
    id: 5,
    naverId: 'nv-seed-1',
    nickname: '새싹팬',
    tier: 'seed',
    points: 18,
    createdAt: '2025-04-29T19:20:00.000Z',
  },
];
