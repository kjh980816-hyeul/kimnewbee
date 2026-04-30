export interface NoticeListItem {
  id: number;
  title: string;
  author: string;
  createdAt: string;
  viewCount: number;
}

export interface Notice extends NoticeListItem {
  content: string;
  updatedAt: string;
}

export interface NoticeListResponse {
  data: NoticeListItem[];
  total: number;
}
