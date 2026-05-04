import { http, HttpResponse } from 'msw';
import { readMockUser } from './auth';

const API_URL = import.meta.env.VITE_API_URL;

const ALLOWED_MIMES = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/gif']);
const MAX_BYTES = 10 * 1024 * 1024;

let mockFileSeed = 1;

export const uploadHandlers = [
  http.post(`${API_URL}/api/files`, async ({ request }) => {
    const user = readMockUser();
    if (!user) {
      return HttpResponse.json({ message: '로그인이 필요해요' }, { status: 401 });
    }
    const form = await request.formData();
    const file = form.get('file');
    if (!(file instanceof File) || file.size === 0) {
      return HttpResponse.json({ message: '파일이 비어있어요' }, { status: 400 });
    }
    if (file.size > MAX_BYTES) {
      return HttpResponse.json({ message: '파일이 너무 커요 (10MB 제한)' }, { status: 400 });
    }
    if (!ALLOWED_MIMES.has(file.type)) {
      return HttpResponse.json({ message: '이미지 형식만 허용돼요' }, { status: 400 });
    }
    const seed = mockFileSeed++;
    return HttpResponse.json(
      { url: `https://picsum.photos/seed/upload-${seed}/800/600` },
      { status: 201 },
    );
  }),
];
