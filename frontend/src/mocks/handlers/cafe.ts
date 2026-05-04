import { http, HttpResponse } from 'msw';
import { readMockUser } from './auth';
import type { CafeConfig, CafeConfigUpdateInput } from '@/types/cafe';

const API_URL = import.meta.env.VITE_API_URL;

const cafeStore: CafeConfig = {
  heroBannerUrl: null,
  heroHeadline: '고추밭에 어서오세요 🌶️',
  heroSubtext: '김늉비 팬들이 모이는 초록고추 정원',
  footerText: '© 고추밭 — 김늉비 팬커뮤니티',
  updatedAt: new Date().toISOString(),
};

function isAdmin(): boolean {
  try {
    return globalThis.localStorage?.getItem('mock-is-admin') === 'true';
  } catch {
    return false;
  }
}

export const cafeHandlers = [
  http.get(`${API_URL}/api/cafe/config`, () => {
    return HttpResponse.json(cafeStore);
  }),

  http.put(`${API_URL}/api/admin/cafe/config`, async ({ request }) => {
    if (!readMockUser()) return HttpResponse.json({ message: '로그인이 필요해요' }, { status: 401 });
    if (!isAdmin()) return HttpResponse.json({ message: '관리자 권한이 필요해요' }, { status: 403 });
    const body = (await request.json()) as CafeConfigUpdateInput;
    if (!body.heroHeadline?.trim()) {
      return HttpResponse.json({ code: 'INVALID_REQUEST', message: '헤드라인은 필수' }, { status: 400 });
    }
    cafeStore.heroBannerUrl = body.heroBannerUrl || null;
    cafeStore.heroHeadline = body.heroHeadline;
    cafeStore.heroSubtext = body.heroSubtext || null;
    cafeStore.footerText = body.footerText || null;
    cafeStore.updatedAt = new Date().toISOString();
    return HttpResponse.json(cafeStore);
  }),
];
