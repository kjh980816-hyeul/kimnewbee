import { apiClient } from './client';
import type {
  BoardPostDetail,
  BoardPostListResponse,
  CreateBoardPostInput,
  LikeToggleResponse,
} from '@/types/boardPost';

export async function fetchBoardPosts(slug: string): Promise<BoardPostListResponse> {
  const res = await apiClient.get<BoardPostListResponse>(`/api/board-posts/${slug}`);
  return res.data;
}

export async function fetchBoardPost(slug: string, id: number): Promise<BoardPostDetail> {
  const res = await apiClient.get<BoardPostDetail>(`/api/board-posts/${slug}/${id}`);
  return res.data;
}

export async function createBoardPost(
  slug: string,
  input: CreateBoardPostInput,
): Promise<BoardPostDetail> {
  const res = await apiClient.post<BoardPostDetail>(`/api/board-posts/${slug}`, input);
  return res.data;
}

export async function toggleBoardPostLike(slug: string, id: number): Promise<LikeToggleResponse> {
  const res = await apiClient.post<LikeToggleResponse>(`/api/board-posts/${slug}/${id}/like`);
  return res.data;
}
