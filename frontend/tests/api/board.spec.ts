import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import {
  fetchPublicBoards,
  fetchAdminBoards,
  createBoard,
  updateBoard,
  deleteBoard,
  reorderBoards,
} from '@/api/board';
import { boardFixtures } from '@/mocks/data/boards';
import type { Board, BoardCreateInput, BoardUpdateInput, BoardLayout } from '@/types/board';
import type { Tier } from '@/types/offline';

const API_URL = 'http://localhost:8080';

let store: Board[] = [];
let viewerIsAdmin = true;

const server = setupServer(
  http.get(`${API_URL}/api/boards`, () => {
    const list = [...store].sort((a, b) => a.orderIndex - b.orderIndex).filter((b) => b.active);
    return HttpResponse.json({ data: list, total: list.length });
  }),
  http.get(`${API_URL}/api/admin/boards`, () => {
    if (!viewerIsAdmin) return HttpResponse.json({}, { status: 403 });
    const list = [...store].sort((a, b) => a.orderIndex - b.orderIndex);
    return HttpResponse.json({ data: list, total: list.length });
  }),
  http.post(`${API_URL}/api/admin/boards`, async ({ request }) => {
    if (!viewerIsAdmin) return HttpResponse.json({}, { status: 403 });
    const body = (await request.json()) as BoardCreateInput;
    if (store.some((b) => b.slug === body.slug)) return HttpResponse.json({}, { status: 400 });
    const newBoard: Board = {
      id: 9999,
      slug: body.slug,
      name: body.name,
      layout: body.layout.toLowerCase() as BoardLayout,
      readTier: body.readTier.toLowerCase() as Tier,
      writeTier: body.writeTier.toLowerCase() as Tier,
      orderIndex: store.length,
      active: true,
    };
    store.push(newBoard);
    return HttpResponse.json(newBoard, { status: 201 });
  }),
  http.patch(`${API_URL}/api/admin/boards/:id`, async ({ params, request }) => {
    if (!viewerIsAdmin) return HttpResponse.json({}, { status: 403 });
    const id = Number(params['id']);
    const target = store.find((b) => b.id === id);
    if (!target) return HttpResponse.json({}, { status: 404 });
    const body = (await request.json()) as BoardUpdateInput;
    target.name = body.name;
    target.layout = body.layout.toLowerCase() as BoardLayout;
    target.readTier = body.readTier.toLowerCase() as Tier;
    target.writeTier = body.writeTier.toLowerCase() as Tier;
    target.active = body.active;
    return HttpResponse.json(target);
  }),
  http.delete(`${API_URL}/api/admin/boards/:id`, ({ params }) => {
    if (!viewerIsAdmin) return HttpResponse.json({}, { status: 403 });
    const id = Number(params['id']);
    const idx = store.findIndex((b) => b.id === id);
    if (idx < 0) return HttpResponse.json({}, { status: 404 });
    store.splice(idx, 1);
    return new HttpResponse(null, { status: 204 });
  }),
  http.put(`${API_URL}/api/admin/boards/order`, async ({ request }) => {
    if (!viewerIsAdmin) return HttpResponse.json({}, { status: 403 });
    const body = (await request.json()) as { orderedIds: number[] };
    body.orderedIds.forEach((id, index) => {
      const target = store.find((b) => b.id === id);
      if (target) target.orderIndex = index;
    });
    const sorted = [...store].sort((a, b) => a.orderIndex - b.orderIndex);
    return HttpResponse.json({ data: sorted, total: sorted.length });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  store = boardFixtures.map((b) => ({ ...b }));
  viewerIsAdmin = true;
});

describe('board api', () => {
  it('public list returns active boards in order', async () => {
    const res = await fetchPublicBoards();
    expect(res.total).toBe(boardFixtures.length);
    expect(res.data[0]?.orderIndex).toBe(0);
  });

  it('admin list 403 when not admin', async () => {
    viewerIsAdmin = false;
    await expect(fetchAdminBoards()).rejects.toThrow();
  });

  it('admin list returns all boards', async () => {
    const res = await fetchAdminBoards();
    expect(res.total).toBe(boardFixtures.length);
  });

  it('create persists new board', async () => {
    const created = await createBoard({
      slug: 'new-board',
      name: '신규',
      layout: 'GALLERY',
      readTier: 'SEED',
      writeTier: 'PEPPER',
    });
    expect(created.slug).toBe('new-board');
    expect(created.layout).toBe('gallery');
    expect(created.active).toBe(true);
  });

  it('create rejects duplicate slug', async () => {
    await expect(
      createBoard({
        slug: 'free',
        name: '중복',
        layout: 'LIST',
        readTier: 'SEED',
        writeTier: 'PEPPER',
      }),
    ).rejects.toThrow();
  });

  it('update modifies existing board', async () => {
    const target = store[0];
    if (!target) throw new Error('store empty');
    const updated = await updateBoard(target.id, {
      name: '수정',
      layout: 'CARD',
      readTier: 'PEPPER',
      writeTier: 'CORN',
      active: false,
    });
    expect(updated.name).toBe('수정');
    expect(updated.layout).toBe('card');
    expect(updated.active).toBe(false);
  });

  it('delete removes board', async () => {
    const target = store[1];
    if (!target) throw new Error('store size insufficient');
    await deleteBoard(target.id);
    expect(store.find((b) => b.id === target.id)).toBeUndefined();
  });

  it('reorder updates orderIndex per request order', async () => {
    const reversed = store.map((b) => b.id).reverse();
    const res = await reorderBoards(reversed);
    expect(res.data[0]?.id).toBe(reversed[0]);
  });
});
