<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { createLetter } from '@/api/letter';

const router = useRouter();

const content = ref('');
const submitting = ref(false);
const submitted = ref(false);
const error = ref<string | null>(null);

async function onSubmit(): Promise<void> {
  if (!content.value.trim()) {
    error.value = '편지 내용을 입력해주세요';
    return;
  }

  submitting.value = true;
  error.value = null;

  try {
    await createLetter({ content: content.value });
    submitted.value = true;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '편지 보내기에 실패했어요';
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <button
      type="button"
      class="mb-4 text-sm text-ink-muted hover:text-ink"
      @click="router.back()"
    >
      ← 취소
    </button>

    <h1 class="text-2xl font-bold text-pepper mb-2">늉비에게 편지쓰기</h1>
    <p class="mb-6 text-sm text-ink-muted">
      편지 본문은 밭주인만 읽을 수 있어요. 따뜻한 마음 전해주세요.
    </p>

    <div
      v-if="submitted"
      class="rounded-md bg-surface p-8 text-center max-w-2xl"
    >
      <p class="text-2xl">💌</p>
      <p class="mt-2 text-ink">편지를 보냈어요!</p>
      <p class="mt-1 text-sm text-ink-muted">밭주인이 곧 읽을거에요.</p>
      <div class="mt-4 flex justify-center gap-2">
        <RouterLink
          :to="{ name: 'letters' }"
          class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep"
        >
          편지함으로
        </RouterLink>
      </div>
    </div>
    <form v-else class="space-y-4 max-w-2xl" @submit.prevent="onSubmit">
      <div>
        <label class="block text-sm text-ink-muted mb-1" for="letter-content">편지 내용</label>
        <textarea
          id="letter-content"
          v-model="content"
          rows="14"
          maxlength="2000"
          placeholder="늉비에게 전하고 싶은 말..."
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper resize-y"
        />
        <p class="mt-1 text-xs text-ink-muted">{{ content.length }} / 2000자</p>
      </div>

      <p v-if="error" class="text-cheek text-sm">{{ error }}</p>

      <div class="flex gap-2">
        <button
          type="submit"
          :disabled="submitting"
          class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep disabled:opacity-50"
        >
          {{ submitting ? '보내는 중...' : '편지 보내기' }}
        </button>
        <button
          type="button"
          class="rounded-md border border-border px-4 py-2 text-sm text-ink-muted hover:text-ink"
          @click="router.back()"
        >
          취소
        </button>
      </div>
    </form>
  </main>
</template>
