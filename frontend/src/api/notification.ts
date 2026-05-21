import { apiClient } from './client';
import type {
  NotificationListResponse,
  UnreadCountResponse,
} from '@/types/notification';

export async function fetchNotifications(): Promise<NotificationListResponse> {
  const res = await apiClient.get<NotificationListResponse>('/api/me/notifications');
  return res.data;
}

export async function fetchUnreadCount(): Promise<UnreadCountResponse> {
  const res = await apiClient.get<UnreadCountResponse>('/api/me/notifications/unread-count');
  return res.data;
}

export async function markNotificationRead(id: number): Promise<void> {
  await apiClient.post(`/api/me/notifications/${id}/read`);
}

export async function markAllNotificationsRead(): Promise<void> {
  await apiClient.post('/api/me/notifications/read-all');
}
