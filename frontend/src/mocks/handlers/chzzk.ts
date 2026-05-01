import { http, HttpResponse } from 'msw';
import type { LiveStatus } from '@/types/chzzk';

const API_URL = import.meta.env.VITE_API_URL;

// localStorage 'mock-is-live' = 'true' | 'false' 로 토글. 기본값 'true' (홈 LIVE 배너 보이게).
function isLiveNow(): boolean {
  try {
    const raw = globalThis.localStorage?.getItem('mock-is-live');
    if (raw === 'false') return false;
    return true;
  } catch {
    return true;
  }
}

export const chzzkHandlers = [
  http.get(`${API_URL}/api/chzzk/live`, () => {
    const live = isLiveNow();
    const payload: LiveStatus = live
      ? {
          isLive: true,
          title: '오늘 합방 + 게임 잡담',
          viewerCount: 1842,
          startedAt: new Date(Date.now() - 45 * 60 * 1000).toISOString(),
          channelUrl: 'https://chzzk.naver.com/example-channel',
        }
      : {
          isLive: false,
          title: '',
          viewerCount: 0,
          startedAt: null,
          channelUrl: 'https://chzzk.naver.com/example-channel',
        };
    return HttpResponse.json(payload);
  }),
];
