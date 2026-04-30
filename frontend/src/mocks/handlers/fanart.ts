import { http, HttpResponse } from 'msw';
import { fanartFixtures } from '../data/fanart';
import type { Fanart, FanartListItem, CreateFanartInput } from '@/types/fanart';

const API_URL = import.meta.env.VITE_API_URL;

const fanartStore: Fanart[] = fanartFixtures.map((f) => ({ ...f }));
let nextFanartId = 2000;

export const fanartHandlers = [
  http.get(`${API_URL}/api/fanart`, () => {
    const list: FanartListItem[] = fanartStore.map((f) => ({
      id: f.id,
      title: f.title,
      author: f.author,
      thumbnailUrl: f.thumbnailUrl,
      createdAt: f.createdAt,
      likeCount: f.likeCount,
      commentCount: f.commentCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),

  http.get(`${API_URL}/api/fanart/:id`, ({ params }) => {
    const id = Number(params['id']);
    const fanart = fanartStore.find((f) => f.id === id);
    if (!fanart) {
      return HttpResponse.json({ message: '팬아트를 찾을 수 없어요' }, { status: 404 });
    }
    fanart.viewCount += 1;
    return HttpResponse.json(fanart);
  }),

  http.post(`${API_URL}/api/fanart`, async ({ request }) => {
    const body = (await request.json()) as CreateFanartInput;
    const now = new Date().toISOString();
    const newFanart: Fanart = {
      id: nextFanartId++,
      title: body.title,
      imageUrl: body.imageUrl,
      thumbnailUrl: body.imageUrl,
      content: body.content,
      author: '초록고추',
      createdAt: now,
      updatedAt: now,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      likedByMe: false,
    };
    fanartStore.unshift(newFanart);
    return HttpResponse.json(newFanart, { status: 201 });
  }),

  http.post(`${API_URL}/api/fanart/:id/like`, ({ params }) => {
    const id = Number(params['id']);
    const fanart = fanartStore.find((f) => f.id === id);
    if (!fanart) {
      return HttpResponse.json({ message: '팬아트를 찾을 수 없어요' }, { status: 404 });
    }
    fanart.likedByMe = !fanart.likedByMe;
    fanart.likeCount += fanart.likedByMe ? 1 : -1;
    return HttpResponse.json({ liked: fanart.likedByMe, likeCount: fanart.likeCount });
  }),
];
