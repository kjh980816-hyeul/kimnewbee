import type { Comment } from '@/types/comment';

export const commentFixtures: Comment[] = [
  {
    id: 1,
    postId: 101,
    parentId: null,
    author: '고추대장',
    content: '저도 보면서 빵 터졌어요 ㅋㅋ',
    createdAt: '2026-04-30T22:30:00.000Z',
    likeCount: 3,
    deleted: false,
  },
  {
    id: 2,
    postId: 101,
    parentId: 1,
    author: '고추한입',
    content: '그쵸~ 다음 합방도 기대됩니다',
    createdAt: '2026-04-30T22:35:00.000Z',
    likeCount: 1,
    deleted: false,
  },
  {
    id: 3,
    postId: 101,
    parentId: null,
    author: '풋고추',
    content: '클립 어디서 봐요?',
    createdAt: '2026-04-30T23:00:00.000Z',
    likeCount: 0,
    deleted: false,
  },
  {
    id: 4,
    postId: 102,
    parentId: null,
    author: '옥수수누나',
    content: '비 오는 날 - 적재 추천!',
    createdAt: '2026-04-30T16:00:00.000Z',
    likeCount: 5,
    deleted: false,
  },
];
