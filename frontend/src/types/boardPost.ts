export interface BoardPostListItem {
  id: number;
  title: string;
  author: string;
  authorProfileImage?: string | null;
  mediaUrl?: string | null;
  preview: string;
  createdAt: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
}

export interface BoardPostDetail extends BoardPostListItem {
  content: string;
  authorTier?: string | null;
  updatedAt: string;
  likedByMe: boolean;
}

export interface BoardPostListResponse {
  data: BoardPostListItem[];
  total: number;
}

export interface CreateBoardPostInput {
  title: string;
  content: string;
  mediaUrl?: string;
}

export interface LikeToggleResponse {
  liked: boolean;
  likeCount: number;
}
