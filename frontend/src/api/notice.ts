import { apiClient } from './client';
import type { Notice, NoticeListResponse } from '@/types/notice';

export async function fetchNotices(): Promise<NoticeListResponse> {
  const res = await apiClient.get<NoticeListResponse>('/api/notices');
  return res.data;
}

export async function fetchNotice(id: number): Promise<Notice> {
  const res = await apiClient.get<Notice>(`/api/notices/${id}`);
  return res.data;
}
