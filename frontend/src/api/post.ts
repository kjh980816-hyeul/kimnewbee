import { apiClient } from './client';

// 작성자 본인 또는 관리자(OWNER)가 게시판 종류 무관하게 게시글 삭제(연관 댓글/좋아요도 함께 제거).
export async function deletePost(id: number): Promise<void> {
  await apiClient.delete(`/api/posts/${id}`);
}
