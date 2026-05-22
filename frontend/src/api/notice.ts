import { apiClient } from './client';
import type { Notice, NoticeListResponse, NoticeWriteInput } from '@/types/notice';

export async function fetchNotices(): Promise<NoticeListResponse> {
  const res = await apiClient.get<NoticeListResponse>('/api/notices');
  return res.data;
}

export async function fetchNotice(id: number): Promise<Notice> {
  const res = await apiClient.get<Notice>(`/api/notices/${id}`);
  return res.data;
}

export async function createNotice(input: NoticeWriteInput): Promise<Notice> {
  const res = await apiClient.post<Notice>('/api/notices', input);
  return res.data;
}

export async function updateNotice(id: number, input: NoticeWriteInput): Promise<Notice> {
  const res = await apiClient.patch<Notice>(`/api/notices/${id}`, input);
  return res.data;
}

export async function deleteNotice(id: number): Promise<void> {
  await apiClient.delete(`/api/notices/${id}`);
}
