import { describe, it, expect } from 'vitest';
import { AxiosError } from 'axios';
import { isHttpStatus } from '@/api/error';

function makeAxiosError(status: number): AxiosError {
  const err = new AxiosError('mock');
  err.response = { status, data: {}, statusText: '', headers: {}, config: { headers: {} as never } };
  return err;
}

describe('isHttpStatus', () => {
  it('returns true when status matches', () => {
    expect(isHttpStatus(makeAxiosError(403), 403)).toBe(true);
    expect(isHttpStatus(makeAxiosError(404), 404)).toBe(true);
  });

  it('returns false when status differs', () => {
    expect(isHttpStatus(makeAxiosError(500), 403)).toBe(false);
  });

  it('returns false for non-axios errors', () => {
    expect(isHttpStatus(new Error('generic'), 403)).toBe(false);
    expect(isHttpStatus(undefined, 403)).toBe(false);
    expect(isHttpStatus('string error', 403)).toBe(false);
  });
});
