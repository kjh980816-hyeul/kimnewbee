import { http, HttpResponse } from 'msw';
import { commentFixtures } from '../data/comments';
import { freePostFixtures } from '../data/free';
import type { Comment } from '@/types/comment';

const API_URL = import.meta.env.VITE_API_URL;

const commentStore: Comment[] = commentFixtures.map((c) => ({ ...c }));
let nextCommentId = 1000;

// 게시글의 댓글수 동기화 (mock 한정)
function refreshCommentCount(postId: number): void {
  const post = freePostFixtures.find((p) => p.id === postId);
  if (post) {
    post.commentCount = commentStore.filter((c) => c.postId === postId && !c.deleted).length;
  }
}

export const commentHandlers = [
  http.get(`${API_URL}/api/posts/:postId/comments`, ({ params }) => {
    const postId = Number(params['postId']);
    const comments = commentStore.filter((c) => c.postId === postId);
    return HttpResponse.json(comments);
  }),

  http.post(`${API_URL}/api/posts/:postId/comments`, async ({ params, request }) => {
    const postId = Number(params['postId']);
    const body = (await request.json()) as { parentId: number | null; content: string };
    const newComment: Comment = {
      id: nextCommentId++,
      postId,
      parentId: body.parentId,
      author: '초록고추',
      content: body.content,
      createdAt: new Date().toISOString(),
      likeCount: 0,
      deleted: false,
    };
    commentStore.push(newComment);
    refreshCommentCount(postId);
    return HttpResponse.json(newComment, { status: 201 });
  }),

  http.delete(`${API_URL}/api/comments/:id`, ({ params }) => {
    const id = Number(params['id']);
    const comment = commentStore.find((c) => c.id === id);
    if (!comment) {
      return HttpResponse.json({ message: '댓글을 찾을 수 없어요' }, { status: 404 });
    }
    comment.deleted = true;
    comment.content = '삭제된 댓글이에요';
    refreshCommentCount(comment.postId);
    return new HttpResponse(null, { status: 204 });
  }),
];
