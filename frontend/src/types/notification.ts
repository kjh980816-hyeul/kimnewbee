export type NotificationType = 'COMMENT' | 'REPLY' | 'LIKE' | 'NOTICE' | 'SYSTEM';

export interface NotificationItem {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  link: string | null;
  read: boolean;
  createdAt: string;
}

export interface NotificationListResponse {
  data: NotificationItem[];
  total: number;
}

export interface UnreadCountResponse {
  unreadCount: number;
}
