import { apiClient } from './client';
import type {
  SongRecommendation,
  SongListResponse,
  CreateSongInput,
  VoteToggleResponse,
} from '@/types/song';

export async function fetchSongs(): Promise<SongListResponse> {
  const res = await apiClient.get<SongListResponse>('/api/songs');
  return res.data;
}

export async function createSong(input: CreateSongInput): Promise<SongRecommendation> {
  const res = await apiClient.post<SongRecommendation>('/api/songs', input);
  return res.data;
}

export async function toggleSongVote(id: number): Promise<VoteToggleResponse> {
  const res = await apiClient.post<VoteToggleResponse>(`/api/songs/${id}/vote`);
  return res.data;
}
