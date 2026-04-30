import { apiClient } from './client';
import type { Comment, CreateCommentInput } from '@/types/comment';

export async function fetchComments(postId: number): Promise<Comment[]> {
  const res = await apiClient.get<Comment[]>(`/api/posts/${postId}/comments`);
  return res.data;
}

export async function createComment(input: CreateCommentInput): Promise<Comment> {
  const res = await apiClient.post<Comment>(`/api/posts/${input.postId}/comments`, {
    parentId: input.parentId,
    content: input.content,
  });
  return res.data;
}

export async function deleteComment(id: number): Promise<void> {
  await apiClient.delete(`/api/comments/${id}`);
}
