import { HttpResponse, type HttpResponseResolver } from 'msw';
import { readMockUser } from '../handlers/auth';

interface OwnedPost {
  id: number;
  author: string;
  updatedAt: string;
}

export function createOwnerOnlyPatch<P extends OwnedPost, B>(
  store: P[],
  apply: (post: P, body: B) => void,
): HttpResponseResolver {
  return async ({ params, request }) => {
    const id = Number(params['id']);
    const post = store.find((p) => p.id === id);
    if (!post) {
      return HttpResponse.json({ message: '게시글을 찾을 수 없어요' }, { status: 404 });
    }
    const user = readMockUser();
    if (!user) {
      return HttpResponse.json({ message: '로그인이 필요해요' }, { status: 401 });
    }
    if (post.author !== user.nickname) {
      return HttpResponse.json({ message: '본인만 수정할 수 있어요' }, { status: 403 });
    }
    const body = (await request.json()) as B;
    apply(post, body);
    post.updatedAt = new Date().toISOString();
    return HttpResponse.json(post);
  };
}
