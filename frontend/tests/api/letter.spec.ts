import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import {
  fetchLetters,
  fetchLetter,
  createLetter,
  fetchAdminViewerInfo,
} from '@/api/letter';
import { letterFixtures } from '@/mocks/data/letters';
import type { Letter } from '@/types/letter';

const API_URL = 'http://localhost:8080';

let store: Letter[] = [];
let isAdmin = false;

const server = setupServer(
  http.get(`${API_URL}/api/me/admin`, () => HttpResponse.json({ isAdmin })),
  http.get(`${API_URL}/api/letters`, () => {
    const list = store.map((l) => ({
      id: l.id,
      author: l.author,
      preview: l.preview,
      createdAt: l.createdAt,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),
  http.get(`${API_URL}/api/letters/:id`, ({ params }) => {
    if (!isAdmin) {
      return HttpResponse.json({ message: '관리자만' }, { status: 403 });
    }
    const id = Number(params['id']);
    const l = store.find((x) => x.id === id);
    return l ? HttpResponse.json(l) : HttpResponse.json({}, { status: 404 });
  }),
  http.post(`${API_URL}/api/letters`, async ({ request }) => {
    const body = (await request.json()) as { content: string };
    const now = new Date().toISOString();
    const l: Letter = {
      id: 9999,
      author: '초록고추',
      preview: body.content.slice(0, 30),
      content: body.content,
      createdAt: now,
      updatedAt: now,
      isReadByAdmin: false,
    };
    store.unshift(l);
    return HttpResponse.json(l, { status: 201 });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  store = letterFixtures.map((l) => ({ ...l }));
  isAdmin = false;
});

describe('letter api', () => {
  it('list response excludes content (only metadata + preview)', async () => {
    const res = await fetchLetters();
    expect(res.data[0]).toHaveProperty('preview');
    expect(res.data[0]).not.toHaveProperty('content');
  });

  it('non-admin gets 403 on letter detail', async () => {
    isAdmin = false;
    await expect(fetchLetter(401)).rejects.toThrow();
  });

  it('admin can read letter detail', async () => {
    isAdmin = true;
    const fixture = letterFixtures[0];
    if (!fixture) throw new Error('letterFixtures empty');
    const l = await fetchLetter(fixture.id);
    expect(l.content).toBeTruthy();
  });

  it('anyone can write a letter', async () => {
    isAdmin = false;
    const l = await createLetter({ content: '안녕하세요 늉비님!' });
    expect(l.id).toBe(9999);
    expect(l.preview).toBe('안녕하세요 늉비님!');
    expect(l.isReadByAdmin).toBe(false);
  });

  it('viewer info reflects admin state', async () => {
    isAdmin = true;
    const info = await fetchAdminViewerInfo();
    expect(info.isAdmin).toBe(true);
  });
});
