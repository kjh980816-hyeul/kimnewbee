import type { FreePost } from '@/types/free';

export const freePostFixtures: FreePost[] = [
  {
    id: 101,
    title: '오늘 합방 진짜 재밌었다',
    content:
      '늉비랑 다른 스트리머 합방 어땠나요?\n저는 마지막 게임 너무 웃겼어요.',
    author: '고추한입',
    createdAt: '2026-04-30T22:10:00.000Z',
    updatedAt: '2026-04-30T22:10:00.000Z',
    viewCount: 312,
    likeCount: 24,
    commentCount: 7,
    likedByMe: false,
  },
  {
    id: 102,
    title: '늉비 노래 추천 좀!',
    content:
      '발라드 위주로 부탁드려요. 댓글에 곡 적어주시면 노래책에 정리해드릴게요.',
    author: '초록고추1991',
    createdAt: '2026-04-30T15:20:00.000Z',
    updatedAt: '2026-04-30T15:20:00.000Z',
    viewCount: 156,
    likeCount: 11,
    commentCount: 4,
    likedByMe: false,
  },
  {
    id: 103,
    title: '오프 모임 다음 달 가능?',
    content:
      '서울권에서 모이실 분 계신가요? 일정 맞춰보고 싶어요.',
    author: '고추대장',
    createdAt: '2026-04-29T18:00:00.000Z',
    updatedAt: '2026-04-29T18:00:00.000Z',
    viewCount: 89,
    likeCount: 6,
    commentCount: 2,
    likedByMe: false,
  },
];
