<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { fetchAdminUsers, changeUserTier, adjustUserPoints } from '@/api/admin';
import { fetchMe } from '@/api/me';
import type { AdminUser } from '@/types/admin';
import type { TierInput } from '@/types/board';
import type { Tier } from '@/types/offline';

const users = ref<AdminUser[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const busyId = ref<number | null>(null);
const search = ref('');
const myId = ref<number | null>(null);
const pointDelta = ref<Record<number, string>>({});

const tierMeta: Record<Tier, { label: string; emoji: string; chip: string }> = {
  seed: { label: '새싹', emoji: '🌱', chip: 'bg-pepper/20 text-pepper' },
  pepper: { label: '고추', emoji: '🌶', chip: 'bg-violet/25 text-violet' },
  corn: { label: '옥수수', emoji: '🌽', chip: 'bg-corn/20 text-corn' },
  owner: { label: '발주인', emoji: '👑', chip: 'bg-cheek/20 text-cheek' },
};

const tierOrder: Tier[] = ['seed', 'pepper', 'corn', 'owner'];

const filteredUsers = computed(() => {
  const q = search.value.trim().toLowerCase();
  if (!q) return users.value;
  return users.value.filter(u => u.nickname.toLowerCase().includes(q) || u.naverId.toLowerCase().includes(q));
});

const tierCounts = computed(() => {
  const counts: Record<Tier, number> = { seed: 0, pepper: 0, corn: 0, owner: 0 };
  users.value.forEach(u => { counts[u.tier]++; });
  return counts;
});

onMounted(async () => {
  try {
    const me = await fetchMe();
    myId.value = me.id;
  } catch {
    // 비치명적
  }
  try {
    const res = await fetchAdminUsers();
    users.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '회원 목록을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onTierChange(user: AdminUser, next: Tier): Promise<void> {
  if (user.tier === next) return;
  if (user.id === myId.value && next !== 'owner') {
    alert('본인을 발주인 외 등급으로 변경할 수 없어요');
    // select 값을 원복하기 위해 다시 렌더 트리거
    users.value = [...users.value];
    return;
  }
  busyId.value = user.id;
  try {
    const updated = await changeUserTier(user.id, { tier: next.toUpperCase() as TierInput });
    Object.assign(user, updated);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '등급 변경에 실패했어요';
  } finally {
    busyId.value = null;
  }
}

async function onPointAdjust(user: AdminUser, delta: number): Promise<void> {
  busyId.value = user.id;
  try {
    const updated = await adjustUserPoints(user.id, { delta });
    Object.assign(user, updated);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '포인트 조정에 실패했어요';
  } finally {
    busyId.value = null;
  }
}

async function onPointCustom(user: AdminUser): Promise<void> {
  const raw = pointDelta.value[user.id];
  const delta = Number(raw);
  if (!raw || !Number.isFinite(delta) || delta === 0) return;
  await onPointAdjust(user, delta);
  pointDelta.value[user.id] = '';
}

function formatDate(iso: string): string {
  const d = new Date(iso);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}

function authorInitial(name: string): string {
  return name ? name.charAt(0) : '?';
}
</script>

<template>
  <div class="p-8">
    <header class="mb-6">
      <h1 class="text-4xl font-extrabold text-ink leading-tight font-serif">회원 관리 👥</h1>
      <p class="mt-2 text-sm text-ink-muted">회원 등급 변경 · 포인트 조정 · 활동 이력 확인</p>
    </header>

    <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-5">
      <div
        v-for="t in tierOrder"
        :key="t"
        class="rounded-xl bg-surface border border-border p-3 flex items-center gap-3"
      >
        <span class="text-2xl">{{ tierMeta[t].emoji }}</span>
        <div class="min-w-0">
          <div class="text-[11px] text-ink-muted">{{ tierMeta[t].label }}</div>
          <div class="text-lg font-extrabold text-ink tabular-nums">{{ tierCounts[t] }}</div>
        </div>
      </div>
    </div>

    <div class="mb-4">
      <div class="relative max-w-md">
        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted text-sm">🔍</span>
        <input
          v-model="search"
          type="text"
          placeholder="닉네임 또는 네이버 ID로 검색..."
          class="w-full pl-9 pr-3 py-2 rounded-full bg-surface border border-border text-sm text-ink placeholder:text-ink-muted/60 focus:outline-none focus:border-violet/40"
        />
      </div>
    </div>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek mb-3">{{ error }}</p>

    <div v-else class="glass overflow-hidden">
      <div class="grid grid-cols-[1fr_120px_120px_100px_200px] gap-3 px-5 py-3 text-[11px] text-ink-muted border-b border-border tracking-wide">
        <span>회원</span>
        <span>등급</span>
        <span class="text-right">포인트</span>
        <span>가입일</span>
        <span class="text-right">포인트 조정</span>
      </div>
      <ul v-if="filteredUsers.length > 0" class="divide-y divide-border">
        <li
          v-for="user in filteredUsers"
          :key="user.id"
          class="grid grid-cols-[1fr_120px_120px_100px_200px] gap-3 px-5 py-3 items-center text-sm hover:bg-elevated transition-colors"
        >
          <div class="flex items-center gap-3 min-w-0">
            <span class="w-9 h-9 rounded-full bg-violet/30 flex items-center justify-center text-sm font-bold text-ink shrink-0">
              {{ authorInitial(user.nickname) }}
            </span>
            <div class="min-w-0">
              <div class="text-ink font-semibold truncate flex items-center gap-1.5">
                <span>{{ user.nickname }}</span>
                <span v-if="user.id === myId" class="text-[10px] px-1.5 py-0.5 rounded bg-violet/20 text-violet shrink-0">나</span>
              </div>
              <div class="text-[10px] text-ink-muted font-mono truncate">{{ user.naverId }}</div>
            </div>
          </div>
          <select
            :value="user.tier"
            :disabled="busyId === user.id"
            class="rounded-full bg-elevated border border-border px-3 py-1 text-xs text-ink focus:outline-none focus:border-violet/40"
            @change="onTierChange(user, ($event.target as HTMLSelectElement).value as Tier)"
          >
            <option v-for="t in tierOrder" :key="t" :value="t">
              {{ tierMeta[t].emoji }} {{ tierMeta[t].label }}
            </option>
          </select>
          <div class="text-right">
            <span class="font-bold text-corn tabular-nums">{{ user.points.toLocaleString() }}</span>
            <span class="text-[10px] text-ink-muted ml-0.5">P</span>
          </div>
          <span class="text-xs text-ink-muted whitespace-nowrap">{{ formatDate(user.createdAt) }}</span>
          <div class="flex items-center justify-end gap-1">
            <button
              type="button"
              :disabled="busyId === user.id"
              class="rounded-full bg-pepper/15 text-pepper px-2 py-1 text-[11px] font-semibold hover:bg-pepper/25 disabled:opacity-50 transition-colors"
              @click="onPointAdjust(user, 100)"
            >
              +100
            </button>
            <button
              type="button"
              :disabled="busyId === user.id"
              class="rounded-full bg-cheek/15 text-cheek px-2 py-1 text-[11px] font-semibold hover:bg-cheek/25 disabled:opacity-50 transition-colors"
              @click="onPointAdjust(user, -100)"
            >
              -100
            </button>
            <input
              v-model="pointDelta[user.id]"
              type="number"
              placeholder="±"
              class="w-14 rounded-full bg-elevated border border-border px-2 py-1 text-[11px] text-ink tabular-nums"
              @keyup.enter="onPointCustom(user)"
            />
            <button
              type="button"
              :disabled="busyId === user.id || !pointDelta[user.id]"
              class="rounded-full bg-violet-deep px-2 py-1 text-[11px] font-semibold text-ink hover:bg-violet-deep/80 disabled:opacity-50"
              @click="onPointCustom(user)"
            >적용</button>
          </div>
        </li>
      </ul>
      <p v-else class="px-5 py-10 text-sm text-ink-muted text-center">
        {{ search ? '검색 결과가 없어요' : '아직 회원이 없어요' }}
      </p>
    </div>
  </div>
</template>
