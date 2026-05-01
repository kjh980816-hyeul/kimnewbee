import type { Tier } from './offline';

export interface CurrentUser {
  id: number;
  nickname: string;
  tier: Tier;
  profileImage: string | null;
  createdAt: string;
}

export interface NaverLoginResponse {
  user: CurrentUser;
}
