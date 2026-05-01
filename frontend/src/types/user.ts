import type { Tier } from './offline';

export interface CurrentUser {
  id: number;
  nickname: string;
  tier: Tier;
  points: number;
  profileImage: string | null;
  createdAt: string;
}

export interface NaverLoginResponse {
  user: CurrentUser;
}

export interface UserStats {
  postCount: number;
  commentCount: number;
  likeGivenCount: number;
  attendanceStreak: number;
}
