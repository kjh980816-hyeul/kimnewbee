import { apiClient } from './client';
import type {
  AdminDashboard,
  AdminUser,
  AdminUserListResponse,
  AdjustPointsInput,
  ChangeTierInput,
} from '@/types/admin';

export async function fetchAdminDashboard(): Promise<AdminDashboard> {
  const res = await apiClient.get<AdminDashboard>('/api/admin/dashboard');
  return res.data;
}

export async function fetchAdminUsers(): Promise<AdminUserListResponse> {
  const res = await apiClient.get<AdminUserListResponse>('/api/admin/users');
  return res.data;
}

export async function changeUserTier(id: number, input: ChangeTierInput): Promise<AdminUser> {
  const res = await apiClient.patch<AdminUser>(`/api/admin/users/${id}/tier`, input);
  return res.data;
}

export async function adjustUserPoints(id: number, input: AdjustPointsInput): Promise<AdminUser> {
  const res = await apiClient.patch<AdminUser>(`/api/admin/users/${id}/points`, input);
  return res.data;
}

// 관리자(OWNER) 전용: 게시판 종류 무관하게 게시글 삭제(연관 댓글/좋아요도 함께 제거).
export async function deletePostAsAdmin(id: number): Promise<void> {
  await apiClient.delete(`/api/admin/posts/${id}`);
}
