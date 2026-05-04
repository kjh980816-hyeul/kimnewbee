import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { fetchPets, fetchPet, createPet, updatePet, togglePetLike } from '@/api/pet';
import { petFixtures } from '@/mocks/data/pets';
import type { Pet } from '@/types/pet';

const API_URL = 'http://localhost:8080';

let store: Pet[] = [];

const server = setupServer(
  http.get(`${API_URL}/api/pets`, () => {
    const list = store.map((p) => ({
      id: p.id,
      title: p.title,
      author: p.author,
      thumbnailUrl: p.thumbnailUrl,
      createdAt: p.createdAt,
      likeCount: p.likeCount,
      commentCount: p.commentCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),
  http.get(`${API_URL}/api/pets/:id`, ({ params }) => {
    const id = Number(params['id']);
    const p = store.find((x) => x.id === id);
    return p ? HttpResponse.json(p) : HttpResponse.json({}, { status: 404 });
  }),
  http.post(`${API_URL}/api/pets`, async ({ request }) => {
    const body = (await request.json()) as { title: string; imageUrl: string; content: string };
    const now = new Date().toISOString();
    const p: Pet = {
      id: 9999,
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
    store.unshift(p);
    return HttpResponse.json(p, { status: 201 });
  }),
  http.patch(`${API_URL}/api/pets/:id`, async ({ params, request }) => {
    const id = Number(params['id']);
    const p = store.find((x) => x.id === id);
    if (!p) return HttpResponse.json({}, { status: 404 });
    const body = (await request.json()) as { title: string; imageUrl: string; content: string };
    p.title = body.title;
    p.imageUrl = body.imageUrl;
    p.thumbnailUrl = body.imageUrl;
    p.content = body.content;
    p.updatedAt = new Date().toISOString();
    return HttpResponse.json(p);
  }),
  http.post(`${API_URL}/api/pets/:id/like`, ({ params }) => {
    const id = Number(params['id']);
    const p = store.find((x) => x.id === id);
    if (!p) return HttpResponse.json({}, { status: 404 });
    p.likedByMe = !p.likedByMe;
    p.likeCount += p.likedByMe ? 1 : -1;
    return HttpResponse.json({ liked: p.likedByMe, likeCount: p.likeCount });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  store = petFixtures.map((p) => ({ ...p }));
});

describe('pet api', () => {
  it('list excludes content/imageUrl (lighter payload)', async () => {
    const res = await fetchPets();
    expect(res.total).toBe(petFixtures.length);
    expect(res.data[0]).toHaveProperty('thumbnailUrl');
    expect(res.data[0]).not.toHaveProperty('imageUrl');
    expect(res.data[0]).not.toHaveProperty('content');
  });

  it('detail includes full imageUrl + content', async () => {
    const fixture = petFixtures[0];
    if (!fixture) throw new Error('petFixtures empty');
    const p = await fetchPet(fixture.id);
    expect(p.imageUrl).toBeTruthy();
    expect(p.content).toBeTruthy();
  });

  it('createPet uses imageUrl as both image and thumbnail', async () => {
    const created = await createPet({
      title: '테스트',
      imageUrl: 'https://example.com/cat.png',
      content: '냐옹',
    });
    expect(created.imageUrl).toBe('https://example.com/cat.png');
    expect(created.thumbnailUrl).toBe('https://example.com/cat.png');
  });

  it('updatePet updates title/imageUrl/content (thumbnail follows)', async () => {
    const fixture = petFixtures[0];
    if (!fixture) throw new Error('petFixtures empty');
    const updated = await updatePet(fixture.id, {
      title: '새 제목',
      imageUrl: 'https://new/dog.png',
      content: '귀여움 갱신',
    });
    expect(updated.imageUrl).toBe('https://new/dog.png');
    expect(updated.thumbnailUrl).toBe('https://new/dog.png');
  });

  it('toggle like flips state and adjusts count', async () => {
    const fixture = petFixtures[0];
    if (!fixture) throw new Error('petFixtures empty');
    const initial = fixture.likeCount;
    const r1 = await togglePetLike(fixture.id);
    expect(r1.liked).toBe(true);
    expect(r1.likeCount).toBe(initial + 1);
    const r2 = await togglePetLike(fixture.id);
    expect(r2.liked).toBe(false);
    expect(r2.likeCount).toBe(initial);
  });
});
