import type { Tier } from './offline';

export interface AdminDashboard {
  totalUsers: number;
  totalPosts: number;
  totalComments: number;
  totalLikes: number;
}

export interface AdminUser {
  id: number;
  naverId: string;
  nickname: string;
  tier: Tier;
  points: number;
  createdAt: string;
}

export interface AdminUserListResponse {
  data: AdminUser[];
  total: number;
}

export interface ChangeTierInput {
  tier: 'SEED' | 'PEPPER' | 'CORN' | 'OWNER';
}

export interface AdjustPointsInput {
  delta: number;
}
