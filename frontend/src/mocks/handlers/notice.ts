import { http, HttpResponse } from 'msw';
import { noticeFixtures } from '../data/notices';
import type { NoticeListItem } from '@/types/notice';

const API_URL = import.meta.env.VITE_API_URL;

export const noticeHandlers = [
  http.get(`${API_URL}/api/notices`, () => {
    const list: NoticeListItem[] = noticeFixtures.map((n) => ({
      id: n.id,
      title: n.title,
      author: n.author,
      createdAt: n.createdAt,
      viewCount: n.viewCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),

  http.get(`${API_URL}/api/notices/:id`, ({ params }) => {
    const id = Number(params['id']);
    const notice = noticeFixtures.find((n) => n.id === id);
    if (!notice) {
      return HttpResponse.json({ message: '공지를 찾을 수 없어요' }, { status: 404 });
    }
    return HttpResponse.json(notice);
  }),
];
