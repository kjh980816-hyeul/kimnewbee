import { describe, it, expect } from 'vitest';
import { apiClient } from '@/api/client';

describe('apiClient', () => {
  it('uses VITE_API_URL as baseURL', () => {
    expect(apiClient.defaults.baseURL).toBe(import.meta.env.VITE_API_URL);
    expect(apiClient.defaults.baseURL).toBeTruthy();
  });

  it('sends credentials by default for refresh-token cookie flow', () => {
    expect(apiClient.defaults.withCredentials).toBe(true);
  });

  it('has a 10s default timeout', () => {
    expect(apiClient.defaults.timeout).toBe(10_000);
  });
});
