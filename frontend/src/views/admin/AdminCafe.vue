<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { fetchCafeConfig, updateCafeConfig } from '@/api/cafe';
import { useImageUpload } from '@/composables/useImageUpload';
import { toRef } from 'vue';

const form = reactive({
  heroBannerUrl: '',
  heroHeadline: '',
  heroSubtext: '',
  footerText: '',
});
const loading = ref(true);
const submitting = ref(false);
const error = ref<string | null>(null);
const ok = ref(false);

const heroBannerUrlRef = toRef(form, 'heroBannerUrl');
const { uploading, uploadError, pickAndUpload } = useImageUpload(heroBannerUrlRef);

onMounted(async () => {
  try {
    const cfg = await fetchCafeConfig();
    form.heroBannerUrl = cfg.heroBannerUrl ?? '';
    form.heroHeadline = cfg.heroHeadline;
    form.heroSubtext = cfg.heroSubtext ?? '';
    form.footerText = cfg.footerText ?? '';
  } catch (e) {
    error.value = e instanceof Error ? e.message : '카페 설정을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onSubmit(): Promise<void> {
  if (!form.heroHeadline.trim()) {
    error.value = '헤드라인은 필수예요';
    return;
  }
  submitting.value = true;
  error.value = null;
  ok.value = false;
  try {
    const updated = await updateCafeConfig({ ...form });
    form.heroBannerUrl = updated.heroBannerUrl ?? '';
    form.heroSubtext = updated.heroSubtext ?? '';
    form.footerText = updated.footerText ?? '';
    ok.value = true;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '저장에 실패했어요';
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold text-ink mb-4">카페 꾸미기</h2>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>

    <form v-else class="space-y-4 max-w-2xl" @submit.prevent="onSubmit">
      <div>
        <label class="block text-sm text-ink-muted mb-1" for="cafe-banner">대표 배너 이미지</label>
        <div class="flex gap-2 items-center">
          <input
            id="cafe-banner"
            v-model="form.heroBannerUrl"
            type="url"
            placeholder="https://... (비우면 그라데이션 배경)"
            class="flex-1 rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
          />
          <label
            class="cursor-pointer rounded-md border border-border px-3 py-2 text-sm text-ink-muted hover:text-pepper hover:border-pepper"
            :class="{ 'opacity-50 pointer-events-none': uploading }"
          >
            {{ uploading ? '업로드 중...' : '파일 선택' }}
            <input type="file" accept="image/*" class="hidden" @change="pickAndUpload" />
          </label>
        </div>
        <p v-if="uploadError" class="mt-1 text-xs text-cheek">{{ uploadError }}</p>
        <div v-if="form.heroBannerUrl" class="mt-2 aspect-[3/1] w-full overflow-hidden rounded-md bg-elevated">
          <img :src="form.heroBannerUrl" alt="배너 미리보기" class="w-full h-full object-cover" />
        </div>
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="cafe-headline">헤드라인 (필수, 최대 80자)</label>
        <input
          id="cafe-headline"
          v-model="form.heroHeadline"
          type="text"
          maxlength="80"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink focus:outline-none focus:border-pepper"
        />
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="cafe-subtext">서브 문구 (선택, 최대 200자)</label>
        <input
          id="cafe-subtext"
          v-model="form.heroSubtext"
          type="text"
          maxlength="200"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink focus:outline-none focus:border-pepper"
        />
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="cafe-footer">하단 푸터 (선택, 최대 200자)</label>
        <input
          id="cafe-footer"
          v-model="form.footerText"
          type="text"
          maxlength="200"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink focus:outline-none focus:border-pepper"
        />
      </div>

      <p v-if="error" class="text-cheek text-sm">{{ error }}</p>
      <p v-else-if="ok" class="text-pepper text-sm">저장됐어요</p>

      <button
        type="submit"
        :disabled="submitting"
        class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep disabled:opacity-50"
      >
        {{ submitting ? '저장 중...' : '저장하기' }}
      </button>
    </form>
  </div>
</template>
