import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { fetchCafeConfig, updateCafeConfig } from '@/api/cafe';
import type { CafeConfig, CafeConfigUpdateInput } from '@/types/cafe';

const API_URL = 'http://localhost:8080';

let viewerIsAdmin = true;
let store: CafeConfig = {
  heroBannerUrl: null,
  heroHeadline: '기본',
  heroSubtext: null,
  footerText: null,
  updatedAt: '2026-01-01T00:00:00.000Z',
};

const server = setupServer(
  http.get(`${API_URL}/api/cafe/config`, () => HttpResponse.json(store)),
  http.put(`${API_URL}/api/admin/cafe/config`, async ({ request }) => {
    if (!viewerIsAdmin) return HttpResponse.json({}, { status: 403 });
    const body = (await request.json()) as CafeConfigUpdateInput;
    if (!body.heroHeadline?.trim()) return HttpResponse.json({}, { status: 400 });
    store = {
      heroBannerUrl: body.heroBannerUrl || null,
      heroHeadline: body.heroHeadline,
      heroSubtext: body.heroSubtext || null,
      footerText: body.footerText || null,
      updatedAt: new Date().toISOString(),
    };
    return HttpResponse.json(store);
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  viewerIsAdmin = true;
  store = {
    heroBannerUrl: null,
    heroHeadline: '기본',
    heroSubtext: null,
    footerText: null,
    updatedAt: '2026-01-01T00:00:00.000Z',
  };
});

describe('cafe config api', () => {
  it('public get returns current config', async () => {
    const cfg = await fetchCafeConfig();
    expect(cfg.heroHeadline).toBe('기본');
  });

  it('admin update persists and public get reflects', async () => {
    await updateCafeConfig({
      heroHeadline: '수정',
      heroBannerUrl: 'https://x/b.png',
      heroSubtext: '서브',
      footerText: '푸터',
    });
    const reloaded = await fetchCafeConfig();
    expect(reloaded.heroHeadline).toBe('수정');
    expect(reloaded.heroBannerUrl).toBe('https://x/b.png');
  });

  it('admin update 403 when not admin', async () => {
    viewerIsAdmin = false;
    await expect(
      updateCafeConfig({ heroHeadline: 'x', heroBannerUrl: '', heroSubtext: '', footerText: '' }),
    ).rejects.toThrow();
  });

  it('admin update rejects empty headline', async () => {
    await expect(
      updateCafeConfig({ heroHeadline: '', heroBannerUrl: '', heroSubtext: '', footerText: '' }),
    ).rejects.toThrow();
  });
});
