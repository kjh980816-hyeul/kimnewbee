<script setup lang="ts">
import { computed, onMounted, reactive, ref, toRef } from 'vue';
import { fetchCafeConfig, updateCafeConfig } from '@/api/cafe';
import { useImageUpload } from '@/composables/useImageUpload';
import type { HeroBannerPosition } from '@/types/cafe';

const BANNER_POSITIONS: { value: HeroBannerPosition; label: string; emoji: string }[] = [
  { value: 'top', label: '위', emoji: '⬆' },
  { value: 'center', label: '가운데', emoji: '⏺' },
  { value: 'bottom', label: '아래', emoji: '⬇' },
  { value: 'left', label: '왼쪽', emoji: '⬅' },
  { value: 'right', label: '오른쪽', emoji: '➡' },
];

const form = reactive({
  heroBannerUrl: '',
  heroBannerPosition: 'center' as HeroBannerPosition,
  heroHeadline: '',
  heroSubtext: '',
  footerText: '',
  chzzkChannelId: '',
});
const original = reactive({
  heroBannerUrl: '',
  heroBannerPosition: 'center' as HeroBannerPosition,
  heroHeadline: '',
  heroSubtext: '',
  footerText: '',
  chzzkChannelId: '',
});
const loading = ref(true);
const submitting = ref(false);
const error = ref<string | null>(null);
const ok = ref(false);

const heroBannerUrlRef = toRef(form, 'heroBannerUrl');
const { uploading, uploadError, pickAndUpload } = useImageUpload(heroBannerUrlRef);

const isDirty = computed(() =>
  form.heroBannerUrl !== original.heroBannerUrl ||
  form.heroBannerPosition !== original.heroBannerPosition ||
  form.heroHeadline !== original.heroHeadline ||
  form.heroSubtext !== original.heroSubtext ||
  form.footerText !== original.footerText ||
  form.chzzkChannelId !== original.chzzkChannelId
);

const channelIdValid = computed(() => {
  const v = form.chzzkChannelId.trim();
  if (!v) return true;
  return /^[a-f0-9]{32}$/.test(v);
});

const headlineCount = computed(() => form.heroHeadline.length);
const subtextCount = computed(() => form.heroSubtext.length);
const footerCount = computed(() => form.footerText.length);

onMounted(async () => {
  try {
    const cfg = await fetchCafeConfig();
    form.heroBannerUrl = cfg.heroBannerUrl ?? '';
    form.heroBannerPosition = cfg.heroBannerPosition ?? 'center';
    form.heroHeadline = cfg.heroHeadline;
    form.heroSubtext = cfg.heroSubtext ?? '';
    form.footerText = cfg.footerText ?? '';
    form.chzzkChannelId = cfg.chzzkChannelId ?? '';
    Object.assign(original, form);
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
  if (!channelIdValid.value) {
    error.value = '치지직 채널 ID 형식이 올바르지 않아요 (32자 16진수)';
    return;
  }
  submitting.value = true;
  error.value = null;
  ok.value = false;
  try {
    const updated = await updateCafeConfig({
      ...form,
      chzzkChannelId: form.chzzkChannelId.trim(),
    });
    form.heroBannerUrl = updated.heroBannerUrl ?? '';
    form.heroBannerPosition = updated.heroBannerPosition ?? 'center';
    form.heroSubtext = updated.heroSubtext ?? '';
    form.footerText = updated.footerText ?? '';
    form.chzzkChannelId = updated.chzzkChannelId ?? '';
    Object.assign(original, form);
    ok.value = true;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '저장에 실패했어요';
  } finally {
    submitting.value = false;
  }
}

function resetChanges(): void {
  Object.assign(form, original);
  error.value = null;
  ok.value = false;
}
</script>

<template>
  <div class="p-8">
    <header class="mb-6 flex items-end justify-between gap-4 flex-wrap">
      <div>
        <h1 class="text-4xl font-extrabold text-ink leading-tight">카페 꾸미기 🎨</h1>
        <p class="mt-2 text-sm text-ink-muted">홈 화면의 배너·인사말·푸터를 자유롭게 꾸며보세요</p>
      </div>
      <div class="flex items-center gap-2">
        <span
          v-if="isDirty"
          class="text-[11px] px-2.5 py-1 rounded-full bg-corn/20 text-corn font-semibold"
        >
          • 저장 안 된 변경 있음
        </span>
        <button
          v-if="isDirty"
          type="button"
          class="rounded-full border border-border px-4 py-2 text-sm text-ink-muted hover:text-ink hover:border-ink/40 transition-colors"
          @click="resetChanges"
        >
          취소
        </button>
        <button
          type="button"
          :disabled="submitting || !isDirty"
          class="rounded-full px-6 py-2 text-sm font-bold text-ink transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          :class="isDirty ? 'bg-gradient-to-r from-violet-deep to-violet/60 hover:shadow-lg hover:shadow-violet/20' : 'bg-elevated'"
          @click="onSubmit"
        >
          {{ submitting ? '저장 중...' : '✓ 저장하기' }}
        </button>
      </div>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>

    <div v-else class="grid grid-cols-1 xl:grid-cols-[420px_1fr] gap-6">
      <div class="space-y-5">
        <section class="rounded-3xl bg-gradient-to-br from-surface to-paper/50 border border-border p-6 shadow-xl shadow-black/20">
          <div class="flex items-center gap-2 mb-4">
            <span class="w-7 h-7 rounded-lg bg-violet/20 flex items-center justify-center text-violet text-sm">🖼</span>
            <h3 class="text-sm font-bold text-ink">대표 배너</h3>
            <span class="text-[10px] text-ink-muted">권장 3:1 비율 · 최대 10MB</span>
          </div>

          <label
            class="block relative aspect-[3/1] rounded-2xl border-2 border-dashed border-violet/30 bg-paper/40 overflow-hidden cursor-pointer hover:border-violet/60 hover:bg-paper/60 transition-colors group"
            :class="{ 'opacity-60 pointer-events-none': uploading }"
          >
            <input type="file" accept="image/*" class="hidden" @change="pickAndUpload" />
            <img
              v-if="form.heroBannerUrl"
              :src="form.heroBannerUrl"
              alt="배너 미리보기"
              class="absolute inset-0 w-full h-full object-cover"
              :style="{ objectPosition: form.heroBannerPosition }"
            />
            <div
              v-if="!form.heroBannerUrl"
              class="absolute inset-0 flex flex-col items-center justify-center gap-2 text-center"
            >
              <span class="w-12 h-12 rounded-full bg-violet/20 flex items-center justify-center text-2xl text-violet group-hover:scale-110 transition-transform">+</span>
              <div class="text-xs text-ink">클릭해서 이미지 선택</div>
              <div class="text-[10px] text-ink-muted">또는 끌어다 놓기</div>
            </div>
            <div
              v-else
              class="absolute inset-0 bg-paper/0 group-hover:bg-paper/60 transition-colors flex items-center justify-center opacity-0 group-hover:opacity-100"
            >
              <span class="text-xs text-ink font-semibold">↻ 다른 이미지로 교체</span>
            </div>
            <div
              v-if="uploading"
              class="absolute inset-0 bg-paper/80 flex items-center justify-center text-sm text-violet font-semibold"
            >
              업로드 중...
            </div>
          </label>

          <div v-if="form.heroBannerUrl" class="mt-2 flex items-center gap-2">
            <input
              v-model="form.heroBannerUrl"
              type="url"
              placeholder="https://..."
              class="flex-1 rounded-full bg-elevated border border-border px-3 py-1.5 text-[11px] text-ink-muted font-mono"
            />
            <button
              type="button"
              class="text-[11px] text-cheek hover:underline whitespace-nowrap"
              @click="form.heroBannerUrl = ''"
            >
              제거
            </button>
          </div>

          <p v-if="uploadError" class="mt-2 text-xs text-cheek">{{ uploadError }}</p>

          <div v-if="form.heroBannerUrl" class="mt-4">
            <div class="text-xs font-semibold text-ink mb-2">배너에서 보일 위치</div>
            <div class="flex flex-wrap gap-1.5">
              <button
                v-for="opt in BANNER_POSITIONS"
                :key="opt.value"
                type="button"
                class="rounded-full px-3 py-1.5 text-xs font-semibold border transition-colors"
                :class="form.heroBannerPosition === opt.value
                  ? 'bg-violet-deep border-violet text-ink'
                  : 'bg-elevated border-border text-ink-muted hover:text-ink hover:border-ink/40'"
                @click="form.heroBannerPosition = opt.value"
              >
                {{ opt.emoji }} {{ opt.label }}
              </button>
            </div>
            <p class="mt-2 text-[11px] text-ink-muted">
              세로가 긴 이미지면 '위' / 가로로 긴 풍경은 '가운데' / 인물 사진은 보통 '위'가 잘 보여요.
            </p>
          </div>
        </section>

        <section class="rounded-3xl bg-gradient-to-br from-surface to-paper/50 border border-border p-6 shadow-xl shadow-black/20 space-y-4">
          <div class="flex items-center gap-2">
            <span class="w-7 h-7 rounded-lg bg-corn/20 flex items-center justify-center text-corn text-sm">✏</span>
            <h3 class="text-sm font-bold text-ink">메시지</h3>
          </div>

          <label class="block">
            <div class="flex items-baseline justify-between mb-1.5">
              <span class="text-xs font-semibold text-ink">헤드라인</span>
              <span class="text-[10px] text-ink-muted tabular-nums">{{ headlineCount }} / 80</span>
            </div>
            <input
              v-model="form.heroHeadline"
              type="text"
              maxlength="80"
              placeholder="오늘도 늉비랑 고추밭에 놀자!"
              class="w-full rounded-xl bg-elevated border border-border px-4 py-2.5 text-sm text-ink placeholder:text-ink-muted/50 focus:outline-none focus:border-violet/50 focus:bg-paper transition-colors"
            />
          </label>

          <label class="block">
            <div class="flex items-baseline justify-between mb-1.5">
              <span class="text-xs font-semibold text-ink">서브 문구 <span class="text-ink-muted font-normal">(선택)</span></span>
              <span class="text-[10px] text-ink-muted tabular-nums">{{ subtextCount }} / 200</span>
            </div>
            <input
              v-model="form.heroSubtext"
              type="text"
              maxlength="200"
              placeholder="초록고추 여러분 안녕~"
              class="w-full rounded-xl bg-elevated border border-border px-4 py-2.5 text-sm text-ink placeholder:text-ink-muted/50 focus:outline-none focus:border-violet/50 focus:bg-paper transition-colors"
            />
          </label>

          <label class="block">
            <div class="flex items-baseline justify-between mb-1.5">
              <span class="text-xs font-semibold text-ink">하단 푸터 <span class="text-ink-muted font-normal">(선택)</span></span>
              <span class="text-[10px] text-ink-muted tabular-nums">{{ footerCount }} / 200</span>
            </div>
            <input
              v-model="form.footerText"
              type="text"
              maxlength="200"
              placeholder="© 김늉비 고추밭 · 공식 팬카페"
              class="w-full rounded-xl bg-elevated border border-border px-4 py-2.5 text-sm text-ink placeholder:text-ink-muted/50 focus:outline-none focus:border-violet/50 focus:bg-paper transition-colors"
            />
          </label>
        </section>

        <section class="rounded-3xl bg-gradient-to-br from-surface to-paper/50 border border-border p-6 shadow-xl shadow-black/20 space-y-3">
          <div class="flex items-center gap-2">
            <span class="w-7 h-7 rounded-lg bg-chzzk/20 flex items-center justify-center text-chzzk text-sm">▶</span>
            <h3 class="text-sm font-bold text-ink">치지직 라이브 연동</h3>
            <span class="text-[10px] text-ink-muted">늉비 방송만 홈에 표시</span>
          </div>

          <label class="block">
            <div class="flex items-baseline justify-between mb-1.5">
              <span class="text-xs font-semibold text-ink">치지직 채널 ID</span>
              <span v-if="!channelIdValid" class="text-[10px] text-cheek">32자 16진수만</span>
            </div>
            <input
              v-model="form.chzzkChannelId"
              type="text"
              maxlength="64"
              placeholder="예: a1b2c3d4e5f6789012345678901234ab"
              class="w-full rounded-xl bg-elevated border px-4 py-2.5 text-sm text-ink font-mono placeholder:text-ink-muted/50 focus:outline-none focus:bg-paper transition-colors"
              :class="channelIdValid ? 'border-border focus:border-violet/50' : 'border-cheek/50 focus:border-cheek'"
            />
            <p class="mt-2 text-[11px] text-ink-muted leading-relaxed">
              치지직 채널 페이지 URL의 마지막 부분이에요. 예) <code class="bg-elevated px-1 rounded text-[10px]">chzzk.naver.com/live/<span class="text-violet">a1b2c3...</span></code>
              <br />방송이 켜져 있으면 홈 상단에 자동으로 라이브 배너가 떠요.
              비워두면 라이브 배너가 안 뜨고, 다른 사람 방송은 등록 못 하니까 안심.
            </p>
          </label>
        </section>

        <p v-if="error" class="text-cheek text-sm">⚠ {{ error }}</p>
        <p v-else-if="ok" class="text-pepper text-sm">✓ 저장됐어요. 홈 화면에서 확인해보세요!</p>
      </div>

      <section class="rounded-3xl bg-gradient-to-br from-violet-deep/15 via-surface to-paper border border-violet/20 shadow-2xl shadow-violet/10 overflow-hidden">
        <header class="px-6 py-3 border-b border-border flex items-center justify-between bg-paper/30 backdrop-blur-sm">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-pepper"></span>
            <span class="text-xs font-semibold text-ink">홈 화면 미리보기</span>
          </div>
          <div class="text-[10px] text-ink-muted">corncorntea.com/</div>
        </header>

        <div class="p-6 space-y-4">
          <div class="rounded-2xl border border-border p-4 flex items-center gap-3 bg-elevated">
            <div class="w-10 h-10 rounded-lg bg-violet-deep/30 flex items-center justify-center text-lg">📺</div>
            <div class="text-sm text-ink-muted">늉비가 지금은 자리 비웠어요. 곧 봐요!</div>
          </div>

          <div
            class="relative rounded-2xl overflow-hidden border border-border"
            :class="form.heroBannerUrl ? '' : 'bg-gradient-to-br from-violet-deep/40 via-violet/20 to-corn/20'"
          >
            <img
              v-if="form.heroBannerUrl"
              :src="form.heroBannerUrl"
              alt=""
              class="absolute inset-0 w-full h-full object-cover"
              :style="{ objectPosition: form.heroBannerPosition }"
            />
            <div
              class="relative p-6 min-h-[140px] flex flex-col justify-center"
              :class="form.heroBannerUrl ? 'bg-gradient-to-r from-paper/85 via-paper/40 to-paper/0' : ''"
            >
              <div class="flex items-center gap-1.5 text-xs text-corn font-semibold mb-2">
                <span class="text-base">🌽</span>
                <span>초록고추 여러분 안녕~</span>
              </div>
              <h1 class="text-2xl font-extrabold text-ink leading-tight">
                {{ form.heroHeadline || '오늘도 늉비랑 고추밭에 놀자!' }}
              </h1>
              <p v-if="form.heroSubtext" class="mt-2 text-sm text-ink/80">
                {{ form.heroSubtext }}
              </p>
            </div>
          </div>

          <div class="rounded-xl bg-corn/10 border border-corn/30 px-4 py-2.5 flex items-center gap-3">
            <span class="px-2 py-0.5 rounded bg-corn text-paper text-[10px] font-bold shrink-0">공지</span>
            <span class="text-xs text-ink truncate">[필독] 고추밭 운영 규칙 및 운영 원칙</span>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div v-for="i in 2" :key="i" class="rounded-xl bg-surface border border-border p-3 h-20"></div>
          </div>

          <div
            v-if="form.footerText"
            class="mt-6 pt-4 border-t border-border text-[11px] text-ink-muted text-center"
          >
            {{ form.footerText }}
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
