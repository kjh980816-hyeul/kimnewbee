export type Tier = 'seed' | 'pepper' | 'corn' | 'owner';

export interface OfflineReviewListItem {
  id: number;
  title: string;
  location: string;
  meetupDate: string;
  thumbnailUrl: string;
  preview: string;
  author: string;
  authorProfileImage?: string | null;
  createdAt: string;
  likeCount: number;
  commentCount: number;
}

export interface OfflineReview extends OfflineReviewListItem {
  content: string;
  imageUrl: string;
  authorTier?: string | null;
  viewCount: number;
  updatedAt: string;
  likedByMe: boolean;
}

export interface OfflineReviewListResponse {
  data: OfflineReviewListItem[];
  total: number;
}

export interface CreateOfflineReviewInput {
  title: string;
  location: string;
  meetupDate: string;
  imageUrl: string;
  content: string;
}
