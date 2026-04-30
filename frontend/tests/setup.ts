import { beforeAll } from 'vitest';

beforeAll(() => {
  // 테스트 환경에서 import.meta.env 보정 — vitest는 .env.local 자동 로드 X
  if (!import.meta.env.VITE_API_URL) {
    (import.meta.env as Record<string, string>).VITE_API_URL = 'http://localhost:8080';
  }
});
