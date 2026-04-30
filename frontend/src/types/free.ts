export interface FreePostListItem {
  id: number;
  title: string;
  author: string;
  createdAt: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
}

export interface FreePost extends FreePostListItem {
  content: string;
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
}

export interface LikeToggleResponse {
  liked: boolean;
  likeCount: number;
}
