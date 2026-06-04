<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { fetchLetter, deleteLetter } from '@/api/letter';
import { isHttpStatus, errorMessage } from '@/api/error';
import { useCurrentUser } from '@/composables/useCurrentUser';
import type { Letter } from '@/types/letter';

const route = useRoute();
const router = useRouter();

const letter = ref<Letter | null>(null);
const loading = ref(true);
const accessDenied = ref(false);
const error = ref<string | null>(null);

const { isOwner } = useCurrentUser();
const letterId = computed(() => Number(route.params['id']));

async function handleDelete(): Promise<void> {
  if (!letter.value) return;
  if (!confirm('이 편지를 삭제할까요? 되돌릴 수 없어요.')) return;
  try {
    await deleteLetter(letter.value.id);
    await router.push({ name: 'letters' });
  } catch (e) {
    error.value = errorMessage(e, '삭제에 실패했어요');
  }
}

onMounted(async () => {
  if (Number.isNaN(letterId.value)) {
    error.value = '잘못된 편지 번호예요';
    loading.value = false;
    return;
  }
  try {
    letter.value = await fetchLetter(letterId.value);
  } catch (e) {
    if (isHttpStatus(e, 403)) {
      accessDenied.value = true;
    } else if (isHttpStatus(e, 404)) {
      error.value = '편지를 찾을 수 없어요';
    } else {
      error.value = e instanceof Error ? e.message : '편지를 불러올 수 없어요';
    }
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="p-8 max-w-3xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <RouterLink :to="{ name: 'letters' }" class="hover:text-ink">팬레터</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">편지</span>
    </nav>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <div
      v-else-if="accessDenied"
      class="rounded-2xl bg-elevated border border-border p-8 text-center"
    >
      <p class="text-3xl">🔒</p>
      <p class="mt-3 text-ink">관리자 전용 편지입니다</p>
      <p class="mt-1 text-sm text-ink-muted">
        팬레터 본문은 밭주인(관리자)만 열람할 수 있어요
      </p>
    </div>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <article v-else-if="letter">
      <div class="mb-4 flex items-center justify-between gap-2">
        <span class="inline-block px-4 py-1.5 rounded-full bg-cheek/20 text-cheek text-sm font-semibold">
          ✉ 편지
        </span>
        <button
          v-if="isOwner"
          type="button"
          class="rounded-md border border-cheek/40 px-3 py-1.5 text-xs font-semibold text-cheek hover:bg-cheek hover:text-paper transition-colors"
          @click="handleDelete"
        >
          🗑 삭제
        </button>
      </div>
      <div class="flex items-center gap-3 pb-5 mb-6 border-b border-border">
        <div class="w-11 h-11 rounded-full bg-gradient-to-br from-cheek to-violet flex items-center justify-center text-lg shrink-0">
          💌
        </div>
        <div class="min-w-0">
          <div class="flex items-center gap-2 flex-wrap">
            <span class="text-sm font-semibold text-ink">{{ letter.author }}</span>
            <span
              v-if="letter.isReadByAdmin"
              class="text-[10px] px-1.5 py-0.5 rounded bg-elevated text-ink-muted"
            >
              읽음
            </span>
            <span
              v-else
              class="text-[10px] px-1.5 py-0.5 rounded bg-pepper text-paper font-semibold"
            >
              새 편지
            </span>
          </div>
          <div class="mt-0.5 text-xs text-ink-muted">
            {{ new Date(letter.createdAt).toLocaleDateString('ko-KR') }}
          </div>
        </div>
      </div>
      <div class="whitespace-pre-wrap text-ink leading-relaxed">{{ letter.content }}</div>
    </article>
  </div>
</template>
