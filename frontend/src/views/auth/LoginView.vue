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
  <main class="min-h-screen relative overflow-hidden flex items-center justify-center bg-gradient-to-br from-violet-deep via-paper to-paper">
    <div class="absolute left-[8%] top-[18%] text-6xl opacity-40 select-none animate-pulse">🐹</div>
    <div class="absolute right-[12%] top-[15%] text-5xl opacity-40 rotate-12 select-none">🎼</div>
    <div class="absolute left-[15%] bottom-[18%] text-4xl opacity-30 select-none">🌽</div>
    <div class="absolute right-[10%] bottom-[15%] text-5xl opacity-40 -rotate-6 select-none">🌶</div>
    <div class="absolute left-[40%] top-[8%] text-3xl opacity-20 select-none">✨</div>
    <div class="absolute right-[30%] bottom-[8%] text-3xl opacity-25 select-none">🍃</div>

    <div class="relative w-full max-w-md px-4">
      <div class="rounded-3xl bg-surface/95 backdrop-blur-md border border-border p-10 shadow-2xl">
        <div class="text-center">
          <div class="flex items-center justify-center gap-2 mb-4">
            <span class="text-3xl">🌶</span>
            <h1 class="text-3xl font-extrabold text-ink leading-none">
              김늉비<span class="text-pepper">고추밭</span>
            </h1>
          </div>
          <p class="text-sm text-ink-muted leading-relaxed">
            초록고추 여러분, 어서와요~<br />
            우리아기 부르고노는 하모니카는~
          </p>
        </div>

        <button
          type="button"
          :disabled="submitting"
          class="mt-8 w-full rounded-2xl bg-violet-deep text-ink px-4 py-4 font-bold hover:bg-violet-deep/80 disabled:opacity-50 transition-colors flex items-center justify-center gap-3"
          @click="onLogin"
        >
          <span class="w-6 h-6 rounded bg-naver text-white font-extrabold flex items-center justify-center text-sm">N</span>
          <span>{{ submitting ? '이동 중...' : '네이버로 간편 로그인' }}</span>
        </button>

        <p v-if="error" class="mt-3 text-cheek text-xs text-center">{{ error }}</p>

        <p class="mt-4 text-[11px] text-ink-muted text-center leading-relaxed">
          김늉비 고추밭은 <span class="text-ink">네이버 계정</span>으로만 가입할 수 있어요<br />
          로그인 시 <a href="#" class="underline hover:text-ink">이용약관</a>과 <a href="#" class="underline hover:text-ink">개인정보처리방침</a>에 동의하게 됩니다
        </p>

        <div class="mt-6 pt-5 border-t border-border">
          <p class="text-[11px] font-semibold text-violet mb-2">🌟 첫 방문이세요?</p>
          <ul class="text-[11px] text-ink-muted space-y-1 leading-relaxed">
            <li>· 네이버 로그인 시 닉네임 자동으로 부여돼요</li>
            <li>· 활동에 따라 등급이 올라가요</li>
            <li class="flex items-center gap-1.5">
              <span>· 등급:</span>
              <span class="px-1.5 py-0.5 rounded bg-elevated text-ink-muted">🌱 새싹</span>
              <span>›</span>
              <span class="px-1.5 py-0.5 rounded bg-pepper/20 text-pepper">🌶 고추</span>
              <span>›</span>
              <span class="px-1.5 py-0.5 rounded bg-corn/20 text-corn">🌽 옥수수</span>
              <span>›</span>
              <span class="px-1.5 py-0.5 rounded bg-violet/30 text-violet">👑 밭주인</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </main>
</template>
