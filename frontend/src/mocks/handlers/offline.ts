import { http, HttpResponse } from 'msw';
import { offlineReviewFixtures } from '../data/offline';
import type {
  OfflineReview,
  OfflineReviewListItem,
  CreateOfflineReviewInput,
  Tier,
} from '@/types/offline';

const API_URL = import.meta.env.VITE_API_URL;

const TIER_RANK: Record<Tier, number> = {
  seed: 0,
  pepper: 1,
  corn: 2,
  owner: 3,
};

// Mock 등급: localStorage 'mock-tier'에서 읽음. 인증 단위 도입 전 임시.
function currentTier(): Tier {
  try {
    const raw = globalThis.localStorage?.getItem('mock-tier');
    if (raw === 'seed' || raw === 'pepper' || raw === 'corn' || raw === 'owner') {
      return raw;
    }
  } catch {
    /* ignore */
  }
  return 'pepper';
}

function hasCornAccess(): boolean {
  return TIER_RANK[currentTier()] >= TIER_RANK.corn;
}

const offlineStore: OfflineReview[] = offlineReviewFixtures.map((r) => ({ ...r }));
let nextOfflineId = 7000;

export const offlineHandlers = [
  http.get(`${API_URL}/api/offline`, () => {
    if (!hasCornAccess()) {
      return HttpResponse.json(
        { message: '옥수수 등급 이상만 볼 수 있어요' },
        { status: 403 },
      );
    }
    const list: OfflineReviewListItem[] = offlineStore.map((r) => ({
      id: r.id,
      title: r.title,
      location: r.location,
      meetupDate: r.meetupDate,
      thumbnailUrl: r.thumbnailUrl,
      preview: r.preview,
      author: r.author,
      createdAt: r.createdAt,
      likeCount: r.likeCount,
      commentCount: r.commentCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),

  http.get(`${API_URL}/api/offline/:id`, ({ params }) => {
    if (!hasCornAccess()) {
      return HttpResponse.json(
        { message: '옥수수 등급 이상만 볼 수 있어요' },
        { status: 403 },
      );
    }
    const id = Number(params['id']);
    const review = offlineStore.find((r) => r.id === id);
    if (!review) {
      return HttpResponse.json({ message: '후기를 찾을 수 없어요' }, { status: 404 });
    }
    review.viewCount += 1;
    return HttpResponse.json(review);
  }),

  http.post(`${API_URL}/api/offline`, async ({ request }) => {
    if (!hasCornAccess()) {
      return HttpResponse.json(
        { message: '옥수수 등급 이상만 작성할 수 있어요' },
        { status: 403 },
      );
    }
    const body = (await request.json()) as CreateOfflineReviewInput;
    const now = new Date().toISOString();
    const preview = body.content.slice(0, 80) + (body.content.length > 80 ? '...' : '');
    const newReview: OfflineReview = {
      id: nextOfflineId++,
      title: body.title,
      location: body.location,
      meetupDate: body.meetupDate,
      imageUrl: body.imageUrl,
      thumbnailUrl: body.imageUrl,
      preview,
      content: body.content,
      author: '초록고추',
      createdAt: now,
      updatedAt: now,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      likedByMe: false,
    };
    offlineStore.unshift(newReview);
    return HttpResponse.json(newReview, { status: 201 });
  }),

  http.post(`${API_URL}/api/offline/:id/like`, ({ params }) => {
    if (!hasCornAccess()) {
      return HttpResponse.json(
        { message: '옥수수 등급 이상만 좋아요할 수 있어요' },
        { status: 403 },
      );
    }
    const id = Number(params['id']);
    const review = offlineStore.find((r) => r.id === id);
    if (!review) {
      return HttpResponse.json({ message: '후기를 찾을 수 없어요' }, { status: 404 });
    }
    review.likedByMe = !review.likedByMe;
    review.likeCount += review.likedByMe ? 1 : -1;
    return HttpResponse.json({ liked: review.likedByMe, likeCount: review.likeCount });
  }),
];
