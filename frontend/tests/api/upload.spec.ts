import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { uploadImage } from '@/api/upload';

const API_URL = 'http://localhost:8080';

let viewerLoggedIn = true;

const server = setupServer(
  http.post(`${API_URL}/api/files`, async ({ request }) => {
    if (!viewerLoggedIn) return HttpResponse.json({}, { status: 401 });
    const form = await request.formData();
    const file = form.get('file');
    if (!(file instanceof File) || file.size === 0) {
      return HttpResponse.json({}, { status: 400 });
    }
    if (file.size > 10 * 1024 * 1024) {
      return HttpResponse.json({}, { status: 400 });
    }
    if (!['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(file.type)) {
      return HttpResponse.json({}, { status: 400 });
    }
    return HttpResponse.json(
      { url: `https://picsum.photos/seed/upload-${file.name}/800/600` },
      { status: 201 },
    );
  }),
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
beforeEach(() => {
  viewerLoggedIn = true;
});

describe('upload api', () => {
  it('uploads PNG and returns URL', async () => {
    const file = new File([new Uint8Array([1, 2, 3, 4])], 'cute.png', { type: 'image/png' });
    const res = await uploadImage(file);
    expect(res.url).toMatch(/picsum/);
  });

  it('rejects when not logged in', async () => {
    viewerLoggedIn = false;
    const file = new File([new Uint8Array([1])], 'x.png', { type: 'image/png' });
    await expect(uploadImage(file)).rejects.toThrow();
  });

  it('rejects empty file', async () => {
    const file = new File([], 'empty.png', { type: 'image/png' });
    await expect(uploadImage(file)).rejects.toThrow();
  });

  it('rejects oversized file', async () => {
    const big = new Uint8Array(10 * 1024 * 1024 + 1);
    const file = new File([big], 'big.png', { type: 'image/png' });
    await expect(uploadImage(file)).rejects.toThrow();
  });

  it('rejects disallowed mime type', async () => {
    const file = new File([new Uint8Array([0])], 'evil.exe', { type: 'application/x-msdownload' });
    await expect(uploadImage(file)).rejects.toThrow();
  });
});
