import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { fetchLiveStatus } from '@/api/chzzk';
import type { LiveStatus } from '@/types/chzzk';

const API_URL = 'http://localhost:8080';

let isLive = true;

const server = setupServer(
  http.get(`${API_URL}/api/chzzk/live`, () => {
    const payload: LiveStatus = isLive
      ? {
          isLive: true,
          title: '테스트 라이브',
          viewerCount: 100,
          startedAt: '2026-04-30T20:00:00.000Z',
          channelUrl: 'https://chzzk.naver.com/test',
        }
      : {
          isLive: false,
          title: '',
          viewerCount: 0,
          startedAt: null,
          channelUrl: 'https://chzzk.naver.com/test',
        };
    return HttpResponse.json(payload);
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  isLive = true;
});

describe('chzzk live status api', () => {
  it('returns live payload when streaming', async () => {
    isLive = true;
    const status = await fetchLiveStatus();
    expect(status.isLive).toBe(true);
    expect(status.title).toBeTruthy();
    expect(status.viewerCount).toBeGreaterThan(0);
    expect(status.startedAt).toBeTruthy();
    expect(status.channelUrl).toContain('chzzk.naver.com');
  });

  it('returns offline payload when not streaming', async () => {
    isLive = false;
    const status = await fetchLiveStatus();
    expect(status.isLive).toBe(false);
    expect(status.viewerCount).toBe(0);
    expect(status.startedAt).toBeNull();
    expect(status.channelUrl).toBeTruthy();
  });
});
