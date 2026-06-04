import { ref } from 'vue';
import { fetchPublicBoards } from '@/api/board';
import type { Board, BoardLayout } from '@/types/board';
import type { Tier } from '@/types/offline';

// 공개(active) 게시판 목록을 모듈 단위로 한 번만 불러 캐시한다.
const boards = ref<Board[]>([]);
const loaded = ref(false);
let inflight: Promise<void> | null = null;

// 기본 8개 게시판은 전용 페이지/아이콘이 따로 있다. 나머지(커스텀)는 /board/:slug로.
export const KNOWN_BOARD_ROUTES: Record<string, { route: string; icon: string }> = {
  notice: { route: '/notices', icon: '★' },
  free: { route: '/free', icon: '💬' },
  fanart: { route: '/fanart', icon: '🎨' },
  pets: { route: '/pets', icon: '🐾' },
  songs: { route: '/songs', icon: '🎵' },
  letters: { route: '/letters', icon: '✉' },
  clips: { route: '/clips', icon: '🎬' },
  offline: { route: '/offline', icon: '📸' },
};

const LAYOUT_ICON: Record<BoardLayout, string> = {
  list: '📋',
  gallery: '🖼',
  card: '🎴',
  video: '🎬',
  letter: '✉',
  rank: '🏆',
};

const TIER_ORDER: Tier[] = ['seed', 'pepper', 'corn', 'owner'];

export function tierRank(t: Tier | string | null | undefined): number {
  const idx = TIER_ORDER.indexOf((t ?? 'seed') as Tier);
  return idx < 0 ? 0 : idx;
}

export function isCustomBoard(b: Board): boolean {
  return !(b.slug in KNOWN_BOARD_ROUTES);
}

export function boardLink(b: Board): string {
  return KNOWN_BOARD_ROUTES[b.slug]?.route ?? `/board/${b.slug}`;
}

export function boardIcon(b: Board): string {
  return KNOWN_BOARD_ROUTES[b.slug]?.icon ?? LAYOUT_ICON[b.layout] ?? '📋';
}

function ensureLoaded(): void {
  if (loaded.value || inflight) return;
  inflight = fetchPublicBoards()
    .then((res) => {
      boards.value = res.data;
    })
    .catch(() => {
      boards.value = [];
    })
    .finally(() => {
      loaded.value = true;
      inflight = null;
    });
}

export function useBoards() {
  ensureLoaded();
  return { boards, loaded };
}
