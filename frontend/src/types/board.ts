import type { Tier } from './offline';

export type BoardLayout = 'list' | 'gallery' | 'card' | 'video' | 'letter' | 'rank';

export type BoardLayoutInput = 'LIST' | 'GALLERY' | 'CARD' | 'VIDEO' | 'LETTER' | 'RANK';

export type TierInput = 'SEED' | 'PEPPER' | 'CORN' | 'OWNER';

export interface Board {
  id: number;
  slug: string;
  name: string;
  layout: BoardLayout;
  readTier: Tier;
  writeTier: Tier;
  orderIndex: number;
  active: boolean;
}

export interface BoardListResponse {
  data: Board[];
  total: number;
}

export interface BoardCreateInput {
  slug: string;
  name: string;
  layout: BoardLayoutInput;
  readTier: TierInput;
  writeTier: TierInput;
}

export interface BoardUpdateInput {
  name: string;
  layout: BoardLayoutInput;
  readTier: TierInput;
  writeTier: TierInput;
  active: boolean;
}
