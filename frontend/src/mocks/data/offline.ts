import type { OfflineReview } from '@/types/offline';

export const offlineReviewFixtures: OfflineReview[] = [
  {
    id: 701,
    title: '4월 서울 정모 후기',
    location: '서울 홍대',
    meetupDate: '2026-04-20',
    imageUrl: 'https://picsum.photos/seed/offline-meetup-1/800/600',
    thumbnailUrl: 'https://picsum.photos/seed/offline-meetup-1/600/400',
    preview: '7명이 모였어요. 늉비 음악 같이 듣고 보드게임도 하고...',
    content:
      '7명이 모였어요. 늉비 음악 같이 듣고 보드게임도 하고 정말 즐거웠습니다.\n\n다음 정모는 5월 둘째주로 일정 잡고 있어요. 관심 있으신 분들은 댓글 남겨주세요!',
    author: '오프대장',
    createdAt: '2026-04-21T11:00:00.000Z',
    updatedAt: '2026-04-21T11:00:00.000Z',
    viewCount: 56,
    likeCount: 23,
    commentCount: 8,
    likedByMe: false,
  },
  {
    id: 702,
    title: '부산 번개 - 광안리에서',
    location: '부산 광안리',
    meetupDate: '2026-04-18',
    imageUrl: 'https://picsum.photos/seed/offline-meetup-2/800/600',
    thumbnailUrl: 'https://picsum.photos/seed/offline-meetup-2/600/400',
    preview: '바다 보면서 늉비 라이브 같이 봤어요. 야외 정모는 처음이었는데...',
    content:
      '바다 보면서 늉비 라이브 같이 봤어요. 야외 정모는 처음이었는데 분위기가 너무 좋아서 또 하고 싶어요.',
    author: '부산초록고추',
    createdAt: '2026-04-19T08:30:00.000Z',
    updatedAt: '2026-04-19T08:30:00.000Z',
    viewCount: 34,
    likeCount: 18,
    commentCount: 5,
    likedByMe: false,
  },
];
