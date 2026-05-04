import { http, HttpResponse } from 'msw';
import { readMockUser } from './auth';
import { boardFixtures } from '../data/boards';
import type { Board, BoardCreateInput, BoardLayout, BoardUpdateInput } from '@/types/board';
import type { Tier } from '@/types/offline';

const API_URL = import.meta.env.VITE_API_URL;

const boardStore: Board[] = boardFixtures.map((b) => ({ ...b }));
let nextBoardId = 1000;

function isAdmin(): boolean {
  try {
    return globalThis.localStorage?.getItem('mock-is-admin') === 'true';
  } catch {
    return false;
  }
}

function deny(status: number, message: string) {
  return HttpResponse.json({ message }, { status });
}

function gateOwner() {
  if (!readMockUser()) return deny(401, '로그인이 필요해요');
  if (!isAdmin()) return deny(403, '관리자 권한이 필요해요');
  return null;
}

function sortedAll(): Board[] {
  return [...boardStore].sort((a, b) => a.orderIndex - b.orderIndex || a.id - b.id);
}

export const boardHandlers = [
  http.get(`${API_URL}/api/boards`, () => {
    const list = sortedAll().filter((b) => b.active);
    return HttpResponse.json({ data: list, total: list.length });
  }),

  http.get(`${API_URL}/api/admin/boards`, () => {
    const blocked = gateOwner();
    if (blocked) return blocked;
    const list = sortedAll();
    return HttpResponse.json({ data: list, total: list.length });
  }),

  http.post(`${API_URL}/api/admin/boards`, async ({ request }) => {
    const blocked = gateOwner();
    if (blocked) return blocked;
    const body = (await request.json()) as BoardCreateInput;
    if (!/^[a-z][a-z0-9-]{0,59}$/.test(body.slug)) {
      return deny(400, 'slug은 소문자/숫자/하이픈만 가능합니다');
    }
    if (boardStore.some((b) => b.slug === body.slug)) {
      return deny(400, '이미 존재하는 slug입니다');
    }
    const newBoard: Board = {
      id: nextBoardId++,
      slug: body.slug,
      name: body.name,
      layout: body.layout.toLowerCase() as BoardLayout,
      readTier: body.readTier.toLowerCase() as Tier,
      writeTier: body.writeTier.toLowerCase() as Tier,
      orderIndex: boardStore.length,
      active: true,
    };
    boardStore.push(newBoard);
    return HttpResponse.json(newBoard, { status: 201 });
  }),

  http.patch(`${API_URL}/api/admin/boards/:id`, async ({ params, request }) => {
    const blocked = gateOwner();
    if (blocked) return blocked;
    const id = Number(params['id']);
    const target = boardStore.find((b) => b.id === id);
    if (!target) return deny(404, '게시판을 찾을 수 없어요');
    const body = (await request.json()) as BoardUpdateInput;
    target.name = body.name;
    target.layout = body.layout.toLowerCase() as BoardLayout;
    target.readTier = body.readTier.toLowerCase() as Tier;
    target.writeTier = body.writeTier.toLowerCase() as Tier;
    target.active = body.active;
    return HttpResponse.json(target);
  }),

  http.delete(`${API_URL}/api/admin/boards/:id`, ({ params }) => {
    const blocked = gateOwner();
    if (blocked) return blocked;
    const id = Number(params['id']);
    const idx = boardStore.findIndex((b) => b.id === id);
    if (idx < 0) return deny(404, '게시판을 찾을 수 없어요');
    boardStore.splice(idx, 1);
    return new HttpResponse(null, { status: 204 });
  }),

  http.put(`${API_URL}/api/admin/boards/order`, async ({ request }) => {
    const blocked = gateOwner();
    if (blocked) return blocked;
    const body = (await request.json()) as { orderedIds: number[] };
    if (body.orderedIds.length !== boardStore.length) {
      return deny(400, '잘못된 정렬 요청');
    }
    body.orderedIds.forEach((id, index) => {
      const target = boardStore.find((b) => b.id === id);
      if (target) target.orderIndex = index;
    });
    return HttpResponse.json({ data: sortedAll(), total: boardStore.length });
  }),
];
