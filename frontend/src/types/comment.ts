export interface Comment {
  id: number;
  postId: number;
  parentId: number | null;
  author: string;
  content: string;
  createdAt: string;
  likeCount: number;
  deleted: boolean;
}

export interface CreateCommentInput {
  postId: number;
  parentId: number | null;
  content: string;
}
