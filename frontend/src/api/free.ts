import { apiClient } from './client';
import type {
  FreePost,
  FreePostListResponse,
  CreateFreePostInput,
  LikeToggleResponse,
} from '@/types/free';

export async function fetchFreePosts(): Promise<FreePostListResponse> {
  const res = await apiClient.get<FreePostListResponse>('/api/free');
  return res.data;
}

export async function fetchFreePost(id: number): Promise<FreePost> {
  const res = await apiClient.get<FreePost>(`/api/free/${id}`);
  return res.data;
}

export async function createFreePost(input: CreateFreePostInput): Promise<FreePost> {
  const res = await apiClient.post<FreePost>('/api/free', input);
  return res.data;
}

export async function toggleFreePostLike(id: number): Promise<LikeToggleResponse> {
  const res = await apiClient.post<LikeToggleResponse>(`/api/free/${id}/like`);
  return res.data;
}
