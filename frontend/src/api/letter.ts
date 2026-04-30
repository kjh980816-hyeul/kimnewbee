import { apiClient } from './client';
import type {
  Letter,
  LetterListResponse,
  CreateLetterInput,
  AdminViewerInfo,
} from '@/types/letter';

export async function fetchLetters(): Promise<LetterListResponse> {
  const res = await apiClient.get<LetterListResponse>('/api/letters');
  return res.data;
}

export async function fetchLetter(id: number): Promise<Letter> {
  const res = await apiClient.get<Letter>(`/api/letters/${id}`);
  return res.data;
}

export async function createLetter(input: CreateLetterInput): Promise<Letter> {
  const res = await apiClient.post<Letter>('/api/letters', input);
  return res.data;
}

export async function fetchAdminViewerInfo(): Promise<AdminViewerInfo> {
  const res = await apiClient.get<AdminViewerInfo>('/api/me/admin');
  return res.data;
}
