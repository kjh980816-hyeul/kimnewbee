<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { loginWithNaver } from '@/api/auth';

const router = useRouter();

const submitting = ref(false);
const error = ref<string | null>(null);

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

async function onLogin(): Promise<void> {
  submitting.value = true;
  error.value = null;
  try {
    if (USE_MOCK) {
      await loginWithNaver();
      await router.push({ name: 'home' });
    } else {
      window.location.href = `${import.meta.env.VITE_API_URL}/api/auth/naver/login`;
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '로그인에 실패했어요';
    submitting.value = false;
  }
}
</script>

<template>
  <main class="min-h-screen relative overflow-hidden bg-gradient-to-br from-violet-deep via-paper to-paper">
    <div class="absolute left-[10%] top-[15%] text-5xl opacity-50 select-none animate-pulse">🐹</div>
    <div class="absolute left-[55%] top-[20%] text-4xl opacity-40 rotate-12 select-none">🎼</div>
    <div class="absolute left-[20%] bottom-[15%] text-3xl opacity-30 select-none">🌽</div>
    <div class="absolute right-[45%] top-[55%] text-4xl opacity-40 -rotate-6 select-none">🌶️</div>

    <div class="absolute right-4 bottom-4 sm:right-16 sm:bottom-16 w-full max-w-md sm:w-[420px]">
      <div class="rounded-3xl bg-elevated/95 backdrop-blur-md border border-border p-10 shadow-2xl">
        <h1 class="text-5xl font-extrabold leading-tight">
          <span class="text-pepper">🌶</span>
          <span class="text-ink">김늉비고</span>
          <br />
          <span class="text-ink pl-14">추밭</span>
        </h1>

        <p class="mt-5 text-sm text-ink-muted leading-relaxed">
          초록고추 여러분, 어서와요~<br />
          🌽 우리아기 불고노는 하모니카는~
        </p>

        <button
          type="button"
          :disabled="submitting"
          class="mt-8 w-full rounded-xl bg-naver text-white px-4 py-3.5 font-bold hover:opacity-90 disabled:opacity-50 transition-opacity flex items-center justify-center gap-3"
          @click="onLogin"
        >
          <span class="w-6 h-6 rounded bg-white text-naver font-extrabold flex items-center justify-center text-sm">N</span>
          <span>{{ submitting ? '이동 중...' : '네이버로 간편 로그인' }}</span>
        </button>

        <p v-if="error" class="mt-3 text-cheek text-xs text-center">{{ error }}</p>

        <p class="mt-5 text-[11px] text-ink-muted text-center leading-relaxed">
          김늉비 고추밭은 <span class="text-ink">네이버 계정</span>으로만 가입할 수 있어요<br />
          로그인 시 <a href="#" class="underline hover:text-ink">이용약관</a>과 <a href="#" class="underline hover:text-ink">개인정보처리방침</a>에 동의하게 됩니다
        </p>
      </div>
    </div>
  </main>
</template>
