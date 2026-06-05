import type { Tier } from './offline';

export interface CurrentUser {
  id: number;
  nickname: string;
  tier: Tier;
  points: number;
  profileImage: string | null;
  nicknameChangedAt: string | null;
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

export interface MyActivityItem {
  id: number;
  boardType: string;
  title: string;
  preview: string;
  link: string;
  thumbnailUrl: string | null;
  commentCount: number;
  likeCount: number;
  createdAt: string;
}
