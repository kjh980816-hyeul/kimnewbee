import { computed, type Ref } from 'vue';
import type { CurrentUser } from '@/types/user';

export function useIsOwner(authorRef: Ref<string | null | undefined>) {
  return computed(() => {
    const author = authorRef.value;
    if (!author) return false;
    try {
      const raw = globalThis.localStorage?.getItem('mock-user');
      if (!raw) return false;
      const user = JSON.parse(raw) as CurrentUser;
      return user.nickname === author;
    } catch {
      return false;
    }
  });
}
