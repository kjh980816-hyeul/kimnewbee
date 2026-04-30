import type { Notice } from '@/types/notice';

export const noticeFixtures: Notice[] = [
  {
    id: 1,
    title: '고추밭 오픈 안내',
    content:
      '안녕하세요 초록고추 여러분!\n김늉비 공식 팬카페 "고추밭"이 오픈했어요. 같이 즐겁게 노는 공간이 되길 바랍니다.',
    author: '밭주인',
    createdAt: '2026-04-26T00:00:00.000Z',
    updatedAt: '2026-04-26T00:00:00.000Z',
    viewCount: 1024,
  },
  {
    id: 2,
    title: '커뮤니티 가이드라인',
    content:
      '서로 존중해주세요. 욕설/혐오 표현, 도배, 외부 광고는 즉시 삭제합니다.\n자세한 내용은 공지를 참고해주세요.',
    author: '밭주인',
    createdAt: '2026-04-27T03:30:00.000Z',
    updatedAt: '2026-04-27T03:30:00.000Z',
    viewCount: 512,
  },
  {
    id: 3,
    title: '5월 첫째주 방송 일정',
    content:
      '월 21시 게임\n수 21시 잡담\n토 22시 합방 — 다른 스트리머와 같이 진행 예정입니다.',
    author: '밭주인',
    createdAt: '2026-04-29T12:15:00.000Z',
    updatedAt: '2026-04-29T12:15:00.000Z',
    viewCount: 256,
  },
];
