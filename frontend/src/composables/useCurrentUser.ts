import { ref, computed } from 'vue';
import { fetchMe } from '@/api/me';
import type { CurrentUser } from '@/types/user';

// /api/me를 모듈 단위로 한 번만 불러 캐시한다(여러 컴포넌트가 동시에 써도 1회 호출).
// 비로그인이면 401 → currentUser null → isOwner false.
const currentUser = ref<CurrentUser | null>(null);
const loaded = ref(false);
let inflight: Promise<void> | null = null;

function ensureLoaded(): void {
  if (loaded.value || inflight) return;
  inflight = fetchMe()
    .then((u) => {
      currentUser.value = u;
    })
    .catch(() => {
      currentUser.value = null;
    })
    .finally(() => {
      loaded.value = true;
      inflight = null;
    });
}

export function useCurrentUser() {
  ensureLoaded();
  const isOwner = computed(() => currentUser.value?.tier === 'owner');
  return { currentUser, isOwner };
}
