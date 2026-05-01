import { apiClient } from './client';
import type {
  OfflineReview,
  OfflineReviewListResponse,
  CreateOfflineReviewInput,
} from '@/types/offline';
import type { LikeToggleResponse } from '@/types/free';

export async function fetchOfflineReviews(): Promise<OfflineReviewListResponse> {
  const res = await apiClient.get<OfflineReviewListResponse>('/api/offline');
  return res.data;
}

export async function fetchOfflineReview(id: number): Promise<OfflineReview> {
  const res = await apiClient.get<OfflineReview>(`/api/offline/${id}`);
  return res.data;
}

export async function createOfflineReview(
  input: CreateOfflineReviewInput,
): Promise<OfflineReview> {
  const res = await apiClient.post<OfflineReview>('/api/offline', input);
  return res.data;
}

export async function toggleOfflineReviewLike(id: number): Promise<LikeToggleResponse> {
  const res = await apiClient.post<LikeToggleResponse>(`/api/offline/${id}/like`);
  return res.data;
}
