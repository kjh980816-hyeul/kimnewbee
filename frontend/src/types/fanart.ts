export interface FanartListItem {
  id: number;
  title: string;
  author: string;
  thumbnailUrl: string;
  createdAt: string;
  likeCount: number;
  commentCount: number;
}

export interface Fanart extends FanartListItem {
  imageUrl: string;
  content: string;
  viewCount: number;
  updatedAt: string;
  likedByMe: boolean;
}

export interface FanartListResponse {
  data: FanartListItem[];
  total: number;
}

export interface CreateFanartInput {
  title: string;
  imageUrl: string;
  content: string;
}
