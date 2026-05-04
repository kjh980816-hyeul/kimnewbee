import { apiClient } from './client';
import type {
  Board,
  BoardCreateInput,
  BoardListResponse,
  BoardUpdateInput,
} from '@/types/board';

export async function fetchPublicBoards(): Promise<BoardListResponse> {
  const res = await apiClient.get<BoardListResponse>('/api/boards');
  return res.data;
}

export async function fetchAdminBoards(): Promise<BoardListResponse> {
  const res = await apiClient.get<BoardListResponse>('/api/admin/boards');
  return res.data;
}

export async function createBoard(input: BoardCreateInput): Promise<Board> {
  const res = await apiClient.post<Board>('/api/admin/boards', input);
  return res.data;
}

export async function updateBoard(id: number, input: BoardUpdateInput): Promise<Board> {
  const res = await apiClient.patch<Board>(`/api/admin/boards/${id}`, input);
  return res.data;
}

export async function deleteBoard(id: number): Promise<void> {
  await apiClient.delete(`/api/admin/boards/${id}`);
}

export async function reorderBoards(orderedIds: number[]): Promise<BoardListResponse> {
  const res = await apiClient.put<BoardListResponse>('/api/admin/boards/order', { orderedIds });
  return res.data;
}
