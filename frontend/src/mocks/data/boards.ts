import type { Board } from '@/types/board';

export const boardFixtures: Board[] = [
  { id: 1, slug: 'notice', name: '공지사항', layout: 'list', readTier: 'seed', writeTier: 'owner', orderIndex: 0, active: true },
  { id: 2, slug: 'free', name: '자유게시판', layout: 'list', readTier: 'seed', writeTier: 'pepper', orderIndex: 1, active: true },
  { id: 3, slug: 'fanart', name: '팬아트', layout: 'gallery', readTier: 'seed', writeTier: 'pepper', orderIndex: 2, active: true },
  { id: 4, slug: 'clips', name: '영상/클립', layout: 'video', readTier: 'seed', writeTier: 'pepper', orderIndex: 3, active: true },
  { id: 5, slug: 'letters', name: '팬레터', layout: 'letter', readTier: 'seed', writeTier: 'pepper', orderIndex: 4, active: true },
  { id: 6, slug: 'pets', name: '반려동물', layout: 'gallery', readTier: 'seed', writeTier: 'pepper', orderIndex: 5, active: true },
  { id: 7, slug: 'songs', name: '노래추천', layout: 'rank', readTier: 'seed', writeTier: 'pepper', orderIndex: 6, active: true },
  { id: 8, slug: 'offline', name: '오프후기', layout: 'card', readTier: 'corn', writeTier: 'corn', orderIndex: 7, active: true },
];
