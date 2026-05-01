<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { createPet } from '@/api/pet';

const router = useRouter();

const title = ref('');
const imageUrl = ref('');
const content = ref('');
const submitting = ref(false);
const error = ref<string | null>(null);

const isImageUrlValid = computed(() => {
  if (!imageUrl.value) return false;
  try {
    const u = new URL(imageUrl.value);
    return u.protocol === 'https:' || u.protocol === 'http:';
  } catch {
    return false;
  }
});

async function onSubmit(): Promise<void> {
  if (!title.value.trim() || !imageUrl.value.trim()) {
    error.value = '제목과 이미지 URL은 필수예요';
    return;
  }
  if (!isImageUrlValid.value) {
    error.value = '이미지 URL이 유효하지 않아요 (http(s)://)';
    return;
  }

  submitting.value = true;
  error.value = null;

  try {
    const pet = await createPet({
      title: title.value,
      imageUrl: imageUrl.value,
      content: content.value,
    });
    await router.push({ name: 'pet-detail', params: { id: pet.id } });
  } catch (e) {
    error.value = e instanceof Error ? e.message : '사진 등록에 실패했어요';
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

    <h1 class="text-2xl font-bold text-pepper mb-6">반려동물 사진 올리기</h1>

    <form class="space-y-4 max-w-2xl" @submit.prevent="onSubmit">
      <div>
        <label class="block text-sm text-ink-muted mb-1" for="pet-title">제목</label>
        <input
          id="pet-title"
          v-model="title"
          type="text"
          maxlength="120"
          placeholder="댕댕이 / 냥냥이 이름 등"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
        />
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="pet-image">이미지 URL</label>
        <input
          id="pet-image"
          v-model="imageUrl"
          type="url"
          placeholder="https://..."
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
        />
        <div v-if="isImageUrlValid" class="mt-2 aspect-square w-48 overflow-hidden rounded-md bg-elevated">
          <img :src="imageUrl" alt="미리보기" class="w-full h-full object-cover" />
        </div>
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="pet-content">한마디 (선택)</label>
        <textarea
          id="pet-content"
          v-model="content"
          rows="4"
          placeholder="자랑할 말, 나이, 입양 스토리 등"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper resize-y"
        />
      </div>

      <p v-if="error" class="text-cheek text-sm">{{ error }}</p>

      <div class="flex gap-2">
        <button
          type="submit"
          :disabled="submitting"
          class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep disabled:opacity-50"
        >
          {{ submitting ? '등록 중...' : '등록하기' }}
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
