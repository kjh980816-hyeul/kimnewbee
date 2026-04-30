import { http, HttpResponse } from 'msw';
import { letterFixtures } from '../data/letters';
import type { Letter, LetterListItem, CreateLetterInput } from '@/types/letter';

const API_URL = import.meta.env.VITE_API_URL;

// Mock 권한: 인증 단위(Phase 1-11/2-2) 도입 전엔 로컬에서 토글하여 UI 동작 확인.
// localStorage에 'mock-is-admin' 키로 'true'/'false' 저장. 기본값은 false.
function isAdmin(): boolean {
  try {
    return globalThis.localStorage?.getItem('mock-is-admin') === 'true';
  } catch {
    return false;
  }
}

const letterStore: Letter[] = letterFixtures.map((l) => ({ ...l }));
let nextLetterId = 4000;

export const letterHandlers = [
  http.get(`${API_URL}/api/me/admin`, () => {
    return HttpResponse.json({ isAdmin: isAdmin() });
  }),

  http.get(`${API_URL}/api/letters`, () => {
    const list: LetterListItem[] = letterStore.map((l) => ({
      id: l.id,
      author: l.author,
      preview: l.preview,
      createdAt: l.createdAt,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),

  http.get(`${API_URL}/api/letters/:id`, ({ params }) => {
    if (!isAdmin()) {
      return HttpResponse.json(
        { message: '관리자만 열람할 수 있어요' },
        { status: 403 },
      );
    }
    const id = Number(params['id']);
    const letter = letterStore.find((l) => l.id === id);
    if (!letter) {
      return HttpResponse.json({ message: '편지를 찾을 수 없어요' }, { status: 404 });
    }
    if (!letter.isReadByAdmin) {
      letter.isReadByAdmin = true;
    }
    return HttpResponse.json(letter);
  }),

  http.post(`${API_URL}/api/letters`, async ({ request }) => {
    const body = (await request.json()) as CreateLetterInput;
    const now = new Date().toISOString();
    const preview = body.content.slice(0, 30) + (body.content.length > 30 ? '...' : '');
    const newLetter: Letter = {
      id: nextLetterId++,
      author: '초록고추',
      preview,
      content: body.content,
      createdAt: now,
      updatedAt: now,
      isReadByAdmin: false,
    };
    letterStore.unshift(newLetter);
    return HttpResponse.json(newLetter, { status: 201 });
  }),
];
