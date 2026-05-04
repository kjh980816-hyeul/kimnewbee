import { http, HttpResponse } from 'msw';
import { freePostFixtures } from '../data/free';
import { readMockUser } from './auth';
import type { FreePost, FreePostListItem, CreateFreePostInput } from '@/types/free';

const API_URL = import.meta.env.VITE_API_URL;

const freePostStore: FreePost[] = freePostFixtures.map((p) => ({ ...p }));
let nextFreePostId = 1000;

export const freeHandlers = [
  http.get(`${API_URL}/api/free`, () => {
    const list: FreePostListItem[] = freePostStore.map((p) => ({
      id: p.id,
      title: p.title,
      author: p.author,
      createdAt: p.createdAt,
      viewCount: p.viewCount,
      likeCount: p.likeCount,
      commentCount: p.commentCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),

  http.get(`${API_URL}/api/free/:id`, ({ params }) => {
    const id = Number(params['id']);
    const post = freePostStore.find((p) => p.id === id);
    if (!post) {
      return HttpResponse.json({ message: '게시글을 찾을 수 없어요' }, { status: 404 });
    }
    post.viewCount += 1;
    return HttpResponse.json(post);
  }),

  http.post(`${API_URL}/api/free`, async ({ request }) => {
    const body = (await request.json()) as CreateFreePostInput;
    const now = new Date().toISOString();
    const newPost: FreePost = {
      id: nextFreePostId++,
      title: body.title,
      content: body.content,
      author: '초록고추',
      createdAt: now,
      updatedAt: now,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      likedByMe: false,
    };
    freePostStore.unshift(newPost);
    return HttpResponse.json(newPost, { status: 201 });
  }),

  http.patch(`${API_URL}/api/free/:id`, async ({ params, request }) => {
    const id = Number(params['id']);
    const post = freePostStore.find((p) => p.id === id);
    if (!post) {
      return HttpResponse.json({ message: '게시글을 찾을 수 없어요' }, { status: 404 });
    }
    const user = readMockUser();
    if (!user) {
      return HttpResponse.json({ message: '로그인이 필요해요' }, { status: 401 });
    }
    if (post.author !== user.nickname) {
      return HttpResponse.json({ message: '본인만 수정할 수 있어요' }, { status: 403 });
    }
    const body = (await request.json()) as CreateFreePostInput;
    post.title = body.title;
    post.content = body.content;
    post.updatedAt = new Date().toISOString();
    return HttpResponse.json(post);
  }),

  http.post(`${API_URL}/api/free/:id/like`, ({ params }) => {
    const id = Number(params['id']);
    const post = freePostStore.find((p) => p.id === id);
    if (!post) {
      return HttpResponse.json({ message: '게시글을 찾을 수 없어요' }, { status: 404 });
    }
    post.likedByMe = !post.likedByMe;
    post.likeCount += post.likedByMe ? 1 : -1;
    return HttpResponse.json({ liked: post.likedByMe, likeCount: post.likeCount });
  }),
];
