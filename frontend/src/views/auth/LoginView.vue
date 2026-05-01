<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { loginWithNaver } from '@/api/auth';

const router = useRouter();

const submitting = ref(false);
const error = ref<string | null>(null);

async function onLogin(): Promise<void> {
  submitting.value = true;
  error.value = null;
  try {
    await loginWithNaver();
    await router.push({ name: 'home' });
  } catch (e) {
    error.value = e instanceof Error ? e.message : '로그인에 실패했어요';
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <main class="min-h-screen bg-paper text-ink grid place-items-center p-8">
    <div class="w-full max-w-sm">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-pepper">고추밭</h1>
        <p class="mt-2 text-ink-muted text-sm">초록고추로 함께해요</p>
      </div>

      <button
        type="button"
        :disabled="submitting"
        class="w-full rounded-md bg-naver text-paper px-4 py-3 text-sm font-medium hover:opacity-90 disabled:opacity-50 transition-opacity"
        @click="onLogin"
      >
        {{ submitting ? '로그인 중...' : 'N 네이버로 시작하기' }}
      </button>

      <p v-if="error" class="mt-4 text-cheek text-sm text-center">{{ error }}</p>

      <p class="mt-8 text-xs text-ink-muted text-center leading-relaxed">
        네이버 로그인 한 번이면 고추밭 회원이 돼요.<br />
        닉네임은 자동 생성되며 마이페이지에서 변경 가능합니다.
      </p>
    </div>
  </main>
</template>
