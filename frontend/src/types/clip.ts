export type ClipSource = 'youtube' | 'chzzk' | 'other';

export interface ClipListItem {
  id: number;
  title: string;
  author: string;
  videoUrl: string;
  source: ClipSource;
  createdAt: string;
  likeCount: number;
  commentCount: number;
}

export interface Clip extends ClipListItem {
  description: string;
  viewCount: number;
  updatedAt: string;
  likedByMe: boolean;
}

export interface ClipListResponse {
  data: ClipListItem[];
  total: number;
}

export interface CreateClipInput {
  title: string;
  videoUrl: string;
  description: string;
}
