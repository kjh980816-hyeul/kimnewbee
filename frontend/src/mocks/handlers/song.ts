import { http, HttpResponse } from 'msw';
import { songFixtures } from '../data/songs';
import type { SongRecommendation, CreateSongInput } from '@/types/song';

const API_URL = import.meta.env.VITE_API_URL;

const songStore: SongRecommendation[] = songFixtures.map((s) => ({ ...s }));
let nextSongId = 6000;

export const songHandlers = [
  http.get(`${API_URL}/api/songs`, () => {
    const sorted = [...songStore].sort((a, b) => b.voteCount - a.voteCount);
    return HttpResponse.json({ data: sorted, total: sorted.length });
  }),

  http.post(`${API_URL}/api/songs`, async ({ request }) => {
    const body = (await request.json()) as CreateSongInput;
    const newSong: SongRecommendation = {
      id: nextSongId++,
      title: body.title,
      artist: body.artist,
      link: body.link,
      submittedBy: '초록고추',
      voteCount: 0,
      votedByMe: false,
      createdAt: new Date().toISOString(),
    };
    songStore.unshift(newSong);
    return HttpResponse.json(newSong, { status: 201 });
  }),

  http.post(`${API_URL}/api/songs/:id/vote`, ({ params }) => {
    const id = Number(params['id']);
    const song = songStore.find((s) => s.id === id);
    if (!song) {
      return HttpResponse.json({ message: '곡을 찾을 수 없어요' }, { status: 404 });
    }
    song.votedByMe = !song.votedByMe;
    song.voteCount += song.votedByMe ? 1 : -1;
    return HttpResponse.json({ voted: song.votedByMe, voteCount: song.voteCount });
  }),
];
