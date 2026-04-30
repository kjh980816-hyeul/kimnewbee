import type { Letter } from '@/types/letter';

export const letterFixtures: Letter[] = [
  {
    id: 401,
    author: '풋고추',
    preview: '늉비님 안녕하세요! 평소에 방송 보면서...',
    content:
      '늉비님 안녕하세요! 평소에 방송 보면서 항상 위로받고 있어요. 늉비님 노래 부르는 모습이 정말 따뜻해요. 앞으로도 응원할게요. 건강 챙기세요!',
    createdAt: '2026-04-30T22:00:00.000Z',
    updatedAt: '2026-04-30T22:00:00.000Z',
    isReadByAdmin: false,
  },
  {
    id: 402,
    author: '옥수수누나',
    preview: '오늘 합방 너무 재밌었어요. 다음에도...',
    content:
      '오늘 합방 너무 재밌었어요. 다음에도 또 같이 하셨으면 좋겠어요. 마지막에 같이 부른 노래 진짜 명장면이었어요!',
    createdAt: '2026-04-29T15:30:00.000Z',
    updatedAt: '2026-04-29T15:30:00.000Z',
    isReadByAdmin: true,
  },
  {
    id: 403,
    author: '익명의 초록고추',
    preview: '항상 응원합니다. 무리하지 마세요...',
    content:
      '항상 응원합니다. 무리하지 마세요. 늉비님이 있어서 행복해요.',
    createdAt: '2026-04-28T11:00:00.000Z',
    updatedAt: '2026-04-28T11:00:00.000Z',
    isReadByAdmin: true,
  },
];
