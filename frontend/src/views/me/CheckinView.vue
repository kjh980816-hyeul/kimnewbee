<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchAttendanceStatus, postCheckIn } from '@/api/attendance';
import { isHttpStatus } from '@/api/error';
import type { AttendanceStatus } from '@/types/attendance';

const status = ref<AttendanceStatus | null>(null);
const loading = ref(true);
const submitting = ref(false);
const notLoggedIn = ref(false);
const error = ref<string | null>(null);
const successMessage = ref<string | null>(null);

const today = computed(() => (status.value ? new Date(status.value.today) : new Date()));
const monthLabel = computed(() => `${today.value.getFullYear()}년 ${today.value.getMonth() + 1}월`);
const daysInMonth = computed(() => new Date(today.value.getFullYear(), today.value.getMonth() + 1, 0).getDate());
const firstWeekday = computed(() => new Date(today.value.getFullYear(), today.value.getMonth(), 1).getDay());

const todayDate = computed(() => today.value.getDate());

const attendedSet = computed(() => {
  const set = new Set<number>();
  status.value?.monthDates.forEach(iso => {
    const d = new Date(iso);
    if (d.getFullYear() === today.value.getFullYear() && d.getMonth() === today.value.getMonth()) {
      set.add(d.getDate());
    }
  });
  return set;
});

const monthAttendedCount = computed(() => attendedSet.value.size);

onMounted(load);

async function load(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    status.value = await fetchAttendanceStatus();
    notLoggedIn.value = false;
  } catch (e) {
    if (isHttpStatus(e, 401)) {
      notLoggedIn.value = true;
    } else {
      error.value = e instanceof Error ? e.message : '출석 정보를 불러올 수 없어요';
    }
  } finally {
    loading.value = false;
  }
}

async function onCheckIn(): Promise<void> {
  if (submitting.value) return;
  submitting.value = true;
  error.value = null;
  successMessage.value = null;
  try {
    const result = await postCheckIn();
    if (result.alreadyCheckedIn) {
      successMessage.value = '오늘은 이미 출석했어요!';
    } else {
      successMessage.value = `+${result.pointsAwarded}P · ${result.currentStreak}일 연속 출석 중`;
    }
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '출석 처리에 실패했어요';
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="p-8">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">출석체크</span>
    </nav>

    <header class="mb-6">
      <h1 class="text-4xl font-extrabold text-ink leading-tight font-serif">출석체크 ✅</h1>
      <p class="mt-2 text-sm text-ink-muted">매일 한 번씩 도장 찍고 포인트 받아가요</p>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>

    <div
      v-else-if="notLoggedIn"
      class="glass p-12 text-center"
    >
      <p class="text-ink mb-4">로그인이 필요해요</p>
      <RouterLink :to="{ name: 'login' }" class="btn-primary inline-block">네이버 로그인</RouterLink>
    </div>

    <template v-else-if="status">
      <section class="grid grid-cols-1 lg:grid-cols-[420px_1fr] gap-5 max-w-5xl">
        <div class="rounded-3xl p-7 shadow-xl shadow-black/20" style="background: linear-gradient(150deg, rgba(95, 199, 107, 0.22), rgba(242, 212, 90, 0.1), rgba(28, 28, 31, 0.7)); border: 1px solid rgba(95, 199, 107, 0.25)">
          <div class="text-xs font-semibold mb-2" style="color: var(--green-bright)">연속 출석</div>
          <div class="flex items-baseline gap-2">
            <span class="text-6xl font-extrabold text-ink tabular-nums">{{ status.currentStreak }}</span>
            <span class="text-2xl font-bold text-ink-muted">일</span>
          </div>
          <p class="mt-3 text-xs text-ink-muted">이번 달 {{ monthAttendedCount }}일 출석 · 매일 +5P</p>

          <button
            type="button"
            :disabled="submitting || status.checkedInToday"
            class="mt-6 w-full rounded-2xl px-4 py-3.5 text-base font-bold transition-all disabled:cursor-not-allowed"
            :class="
              status.checkedInToday
                ? 'bg-pepper/20 text-pepper'
                : 'bg-gradient-to-r from-green to-green-mid text-paper hover:shadow-lg hover:shadow-pepper/30 disabled:opacity-50'
            "
            @click="onCheckIn"
          >
            <span v-if="status.checkedInToday">✓ 오늘 출석 완료</span>
            <span v-else-if="submitting">도장 찍는 중...</span>
            <span v-else>🌶 오늘 출석 도장 찍기</span>
          </button>

          <p v-if="successMessage" class="mt-3 text-xs text-pepper text-center">✓ {{ successMessage }}</p>
          <p v-if="error" class="mt-3 text-xs text-cheek text-center">⚠ {{ error }}</p>
        </div>

        <div class="glass rounded-3xl p-6 shadow-xl shadow-black/20">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-base font-bold text-ink">{{ monthLabel }}</h2>
            <span class="text-xs text-ink-muted">{{ monthAttendedCount }} / {{ daysInMonth }}일</span>
          </div>

          <div class="grid grid-cols-7 gap-1.5 text-center text-[10px] text-ink-muted mb-2">
            <span>일</span>
            <span>월</span>
            <span>화</span>
            <span>수</span>
            <span>목</span>
            <span>금</span>
            <span>토</span>
          </div>

          <div class="grid grid-cols-7 gap-1.5">
            <div v-for="i in firstWeekday" :key="`pad-${i}`" class="aspect-square"></div>
            <div
              v-for="d in daysInMonth"
              :key="d"
              class="aspect-square rounded-lg flex items-center justify-center text-xs font-semibold transition-colors"
              :class="
                attendedSet.has(d)
                  ? 'bg-gradient-to-br from-green to-green-mid text-paper'
                  : d === todayDate
                    ? 'border-2 border-pepper text-pepper bg-elevated'
                    : d < todayDate
                      ? 'bg-elevated text-ink-muted/60'
                      : 'bg-paper text-ink-muted/40 border border-border'
              "
            >
              <span v-if="attendedSet.has(d)">🌶</span>
              <span v-else>{{ d }}</span>
            </div>
          </div>

          <div class="mt-4 pt-4 border-t border-border flex items-center gap-4 text-[11px] text-ink-muted">
            <span class="flex items-center gap-1.5">
              <span class="w-3 h-3 rounded bg-gradient-to-br from-green to-green-mid"></span>
              출석함
            </span>
            <span class="flex items-center gap-1.5">
              <span class="w-3 h-3 rounded border-2 border-pepper"></span>
              오늘
            </span>
            <span class="flex items-center gap-1.5">
              <span class="w-3 h-3 rounded bg-elevated"></span>
              빠진 날
            </span>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>
