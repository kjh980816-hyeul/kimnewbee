// 자유게시판 분류. '전체'는 필터 전용(저장 안 됨).
export const FREE_CATEGORIES = ['잡담', '질문', '후기', '정보'] as const;
export type FreeCategory = (typeof FREE_CATEGORIES)[number];
export const FREE_FILTER_TABS = ['전체', ...FREE_CATEGORIES] as const;

export interface FreePostListItem {
  id: number;
  title: string;
  author: string;
  authorProfileImage?: string | null;
  category?: string | null;
  createdAt: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
}

export interface FreePost extends FreePostListItem {
  content: string;
  authorTier?: string | null;
  updatedAt: string;
  likedByMe: boolean;
}

export interface FreePostListResponse {
  data: FreePostListItem[];
  total: number;
}

export interface CreateFreePostInput {
  title: string;
  content: string;
  category?: string;
}

export interface LikeToggleResponse {
  liked: boolean;
  likeCount: number;
}
