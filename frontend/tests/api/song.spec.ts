import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { fetchSongs, createSong, toggleSongVote } from '@/api/song';
import { songFixtures } from '@/mocks/data/songs';
import type { SongRecommendation } from '@/types/song';

const API_URL = 'http://localhost:8080';

let store: SongRecommendation[] = [];

const server = setupServer(
  http.get(`${API_URL}/api/songs`, () => {
    const sorted = [...store].sort((a, b) => b.voteCount - a.voteCount);
    return HttpResponse.json({ data: sorted, total: sorted.length });
  }),
  http.post(`${API_URL}/api/songs`, async ({ request }) => {
    const body = (await request.json()) as { title: string; artist: string; link: string };
    const s: SongRecommendation = {
      id: 9999,
      title: body.title,
      artist: body.artist,
      link: body.link,
      submittedBy: '초록고추',
      voteCount: 0,
      votedByMe: false,
      createdAt: new Date().toISOString(),
    };
    store.unshift(s);
    return HttpResponse.json(s, { status: 201 });
  }),
  http.post(`${API_URL}/api/songs/:id/vote`, ({ params }) => {
    const id = Number(params['id']);
    const s = store.find((x) => x.id === id);
    if (!s) return HttpResponse.json({}, { status: 404 });
    s.votedByMe = !s.votedByMe;
    s.voteCount += s.votedByMe ? 1 : -1;
    return HttpResponse.json({ voted: s.votedByMe, voteCount: s.voteCount });
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  store = songFixtures.map((s) => ({ ...s }));
});

describe('song api', () => {
  it('list returns songs sorted by voteCount desc', async () => {
    const res = await fetchSongs();
    for (let i = 0; i < res.data.length - 1; i++) {
      const a = res.data[i];
      const b = res.data[i + 1];
      if (!a || !b) throw new Error('songs missing');
      expect(a.voteCount).toBeGreaterThanOrEqual(b.voteCount);
    }
  });

  it('createSong returns new entry with voteCount 0', async () => {
    const created = await createSong({
      title: '신곡',
      artist: '신인',
      link: 'https://www.youtube.com/watch?v=new',
    });
    expect(created.voteCount).toBe(0);
    expect(created.votedByMe).toBe(false);
    expect(created.title).toBe('신곡');
  });

  it('toggleSongVote increments and decrements correctly', async () => {
    const fixture = songFixtures[0];
    if (!fixture) throw new Error('songFixtures empty');
    const initial = fixture.voteCount;

    const r1 = await toggleSongVote(fixture.id);
    expect(r1.voted).toBe(true);
    expect(r1.voteCount).toBe(initial + 1);

    const r2 = await toggleSongVote(fixture.id);
    expect(r2.voted).toBe(false);
    expect(r2.voteCount).toBe(initial);
  });

  it('vote on nonexistent song throws', async () => {
    await expect(toggleSongVote(999999)).rejects.toThrow();
  });
});
