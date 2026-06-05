<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { fetchAdminUsers } from '@/api/admin';
import type { AdminUser } from '@/types/admin';
import type { Tier } from '@/types/offline';

const users = ref<AdminUser[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const tiers: Tier[] = ['seed', 'pepper', 'corn', 'owner'];
const TIER_META: Record<Tier, { label: string; emoji: string; color: string; bg: string }> = {
  seed: { label: '새싹', emoji: '🌱', color: 'text-green-400', bg: 'bg-green-400' },
  pepper: { label: '고추', emoji: '🌶', color: 'text-pepper', bg: 'bg-pepper' },
  corn: { label: '옥수수', emoji: '🌽', color: 'text-corn', bg: 'bg-corn' },
  owner: { label: '밭주인', emoji: '👑', color: 'text-violet', bg: 'bg-violet' },
};

onMounted(async () => {
  try {
    const u = await fetchAdminUsers();
    users.value = u.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '통계를 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

interface DailyPoint {
  date: string;
  label: string;
  count: number;
}

const DAYS = 30;

const signupTrend = computed<DailyPoint[]>(() => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const buckets: DailyPoint[] = [];
  for (let i = DAYS - 1; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const key = d.toISOString().slice(0, 10);
    buckets.push({
      date: key,
      label: `${d.getMonth() + 1}/${d.getDate()}`,
      count: 0,
    });
  }
  const indexByDate = new Map(buckets.map((p, i) => [p.date, i]));
  users.value.forEach((u) => {
    const key = u.createdAt.slice(0, 10);
    const idx = indexByDate.get(key);
    if (idx !== undefined) {
      buckets[idx]!.count++;
    }
  });
  return buckets;
});

const maxSignup = computed(() => Math.max(1, ...signupTrend.value.map((p) => p.count)));

const totalSignupInRange = computed(() => signupTrend.value.reduce((s, p) => s + p.count, 0));

const chartPoints = computed(() => {
  const w = 600;
  const h = 160;
  const pad = 8;
  const innerW = w - pad * 2;
  const innerH = h - pad * 2;
  const max = maxSignup.value;
  return signupTrend.value.map((p, i) => {
    const x = pad + (i * innerW) / (DAYS - 1);
    const y = pad + innerH - (p.count / max) * innerH;
    return { x, y, ...p };
  });
});

const polyline = computed(() => chartPoints.value.map((p) => `${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' '));

const tierCounts = computed(() => {
  const c: Record<Tier, number> = { seed: 0, pepper: 0, corn: 0, owner: 0 };
  users.value.forEach((u) => c[u.tier]++);
  return c;
});
</script>

<template>
  <div class="p-8">
    <header class="mb-6">
      <h1 class="text-4xl font-extrabold text-ink leading-tight font-serif">통계 📈</h1>
      <p class="mt-2 text-sm text-ink-muted">최근 {{ DAYS }}일 추이와 누적 데이터를 확인할 수 있어요</p>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>

    <template v-else>
      <section class="glass p-5 mb-6">
        <div class="flex items-center justify-between mb-3">
          <div>
            <h2 class="text-sm font-bold text-ink">📊 일별 신규 가입자 (최근 {{ DAYS }}일)</h2>
            <p class="text-[11px] text-ink-muted mt-0.5">최대 {{ maxSignup }}명 / 일 · 총 {{ totalSignupInRange }}명</p>
          </div>
        </div>

        <div class="relative w-full" style="height: 180px;">
          <svg viewBox="0 0 600 160" preserveAspectRatio="none" class="w-full h-full" aria-label="신규 가입자 추이">
            <line x1="8" y1="152" x2="592" y2="152" stroke="currentColor" stroke-opacity="0.2" stroke-width="1" />
            <line x1="8" y1="80" x2="592" y2="80" stroke="currentColor" stroke-opacity="0.1" stroke-width="1" stroke-dasharray="2,3" />
            <line x1="8" y1="8" x2="592" y2="8" stroke="currentColor" stroke-opacity="0.1" stroke-width="1" stroke-dasharray="2,3" />

            <polyline
              :points="polyline"
              fill="none"
              stroke="rgb(139, 92, 246)"
              stroke-width="2"
              stroke-linejoin="round"
              stroke-linecap="round"
            />

            <circle
              v-for="(p, i) in chartPoints"
              :key="i"
              :cx="p.x"
              :cy="p.y"
              :r="p.count > 0 ? 3 : 1.5"
              :fill="p.count > 0 ? 'rgb(139, 92, 246)' : 'rgba(139, 92, 246, 0.3)'"
            >
              <title>{{ p.label }}: {{ p.count }}명</title>
            </circle>
          </svg>
        </div>

        <div class="grid grid-cols-6 sm:grid-cols-10 md:grid-cols-15 gap-0 mt-2 text-[9px] text-ink-muted/70 tabular-nums">
          <span
            v-for="(p, i) in chartPoints"
            :key="i"
            class="text-center"
            :class="i % 5 === 0 ? '' : 'opacity-0'"
          >{{ p.label }}</span>
        </div>
      </section>

      <section class="glass p-5">
        <h2 class="text-sm font-bold text-ink mb-4">🏅 등급별 분포 ({{ users.length }}명)</h2>
        <div class="space-y-3">
          <div v-for="t in tiers" :key="t" class="flex items-center gap-3">
            <span class="w-20 text-xs flex items-center gap-1 shrink-0">
              <span class="text-base">{{ TIER_META[t].emoji }}</span>
              <span :class="TIER_META[t].color">{{ TIER_META[t].label }}</span>
            </span>
            <div class="flex-1 h-3 rounded-full bg-elevated overflow-hidden">
              <div
                class="h-full transition-all"
                :class="TIER_META[t].bg"
                :style="{
                  width: users.length > 0 ? (tierCounts[t] / users.length * 100) + '%' : '0%',
                  opacity: 0.7,
                }"
              ></div>
            </div>
            <span class="text-xs text-ink tabular-nums w-16 text-right shrink-0">
              {{ tierCounts[t] }}<span class="text-ink-muted text-[10px] ml-0.5">명</span>
              <span class="text-ink-muted text-[10px] ml-1">
                ({{ users.length > 0 ? Math.round(tierCounts[t] / users.length * 100) : 0 }}%)
              </span>
            </span>
          </div>
        </div>
        <p v-if="users.length === 0" class="mt-3 text-[11px] text-ink-muted text-center py-4">
          아직 가입자가 없어요
        </p>
      </section>

      <p class="mt-6 text-[10px] text-ink-muted/60 text-center">
        * 게시판별 일별 글 수, 시간대 히트맵 등은 백엔드 통계 API 추가 후 제공 예정
      </p>
    </template>
  </div>
</template>
