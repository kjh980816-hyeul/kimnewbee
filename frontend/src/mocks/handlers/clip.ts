import { http, HttpResponse } from 'msw';
import { clipFixtures } from '../data/clips';
import { createOwnerOnlyPatch } from '../utils/createOwnerOnlyPatch';
import type { Clip, ClipListItem, ClipSource, CreateClipInput } from '@/types/clip';

const API_URL = import.meta.env.VITE_API_URL;

const clipStore: Clip[] = clipFixtures.map((c) => ({ ...c }));
let nextClipId = 3000;

function detectSource(url: string): ClipSource {
  if (/youtube\.com|youtu\.be/.test(url)) return 'youtube';
  if (/chzzk\.naver\.com/.test(url)) return 'chzzk';
  return 'other';
}

export const clipHandlers = [
  http.get(`${API_URL}/api/clips`, () => {
    const list: ClipListItem[] = clipStore.map((c) => ({
      id: c.id,
      title: c.title,
      author: c.author,
      videoUrl: c.videoUrl,
      source: c.source,
      createdAt: c.createdAt,
      likeCount: c.likeCount,
      commentCount: c.commentCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),

  http.get(`${API_URL}/api/clips/:id`, ({ params }) => {
    const id = Number(params['id']);
    const clip = clipStore.find((c) => c.id === id);
    if (!clip) {
      return HttpResponse.json({ message: '영상을 찾을 수 없어요' }, { status: 404 });
    }
    clip.viewCount += 1;
    return HttpResponse.json(clip);
  }),

  http.post(`${API_URL}/api/clips`, async ({ request }) => {
    const body = (await request.json()) as CreateClipInput;
    const now = new Date().toISOString();
    const newClip: Clip = {
      id: nextClipId++,
      title: body.title,
      videoUrl: body.videoUrl,
      source: detectSource(body.videoUrl),
      description: body.description,
      author: '초록고추',
      createdAt: now,
      updatedAt: now,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      likedByMe: false,
    };
    clipStore.unshift(newClip);
    return HttpResponse.json(newClip, { status: 201 });
  }),

  http.patch(
    `${API_URL}/api/clips/:id`,
    createOwnerOnlyPatch<Clip, CreateClipInput>(clipStore, (clip, body) => {
      clip.title = body.title;
      clip.videoUrl = body.videoUrl;
      clip.source = detectSource(body.videoUrl);
      clip.description = body.description;
    }),
  ),

  http.post(`${API_URL}/api/clips/:id/like`, ({ params }) => {
    const id = Number(params['id']);
    const clip = clipStore.find((c) => c.id === id);
    if (!clip) {
      return HttpResponse.json({ message: '영상을 찾을 수 없어요' }, { status: 404 });
    }
    clip.likedByMe = !clip.likedByMe;
    clip.likeCount += clip.likedByMe ? 1 : -1;
    return HttpResponse.json({ liked: clip.likedByMe, likeCount: clip.likeCount });
  }),
];
