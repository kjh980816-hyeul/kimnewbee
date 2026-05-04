<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchAdminUsers, changeUserTier, adjustUserPoints } from '@/api/admin';
import type { AdminUser } from '@/types/admin';
import type { Tier } from '@/types/offline';

const users = ref<AdminUser[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const busyId = ref<number | null>(null);

const tierLabel: Record<Tier, string> = {
  seed: '🌱 새싹',
  pepper: '🌶️ 고추',
  corn: '🌽 옥수수',
  owner: '👑 밭주인',
};

const tierOrder: Tier[] = ['seed', 'pepper', 'corn', 'owner'];

onMounted(async () => {
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
  busyId.value = user.id;
  try {
    const updated = await changeUserTier(user.id, { tier: next.toUpperCase() as 'SEED' | 'PEPPER' | 'CORN' | 'OWNER' });
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
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold text-ink mb-4">회원 관리</h2>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek mb-3">{{ error }}</p>

    <table v-if="!loading && users.length > 0" class="w-full text-sm">
      <thead class="bg-surface text-ink-muted">
        <tr>
          <th class="px-3 py-2 text-left">닉네임</th>
          <th class="px-3 py-2 text-left">등급</th>
          <th class="px-3 py-2 text-right">포인트</th>
          <th class="px-3 py-2 text-left">가입일</th>
          <th class="px-3 py-2 text-left">관리</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="user in users"
          :key="user.id"
          class="border-b border-border"
        >
          <td class="px-3 py-2 text-ink">
            {{ user.nickname }}
            <span class="ml-1 text-xs text-ink-muted">({{ user.naverId }})</span>
          </td>
          <td class="px-3 py-2">
            <select
              :value="user.tier"
              :disabled="busyId === user.id"
              class="rounded-md bg-elevated border border-border px-2 py-1 text-ink"
              @change="onTierChange(user, ($event.target as HTMLSelectElement).value as Tier)"
            >
              <option v-for="t in tierOrder" :key="t" :value="t">
                {{ tierLabel[t] }}
              </option>
            </select>
          </td>
          <td class="px-3 py-2 text-right text-corn font-medium">
            {{ user.points.toLocaleString('ko-KR') }}
          </td>
          <td class="px-3 py-2 text-ink-muted">
            {{ new Date(user.createdAt).toLocaleDateString('ko-KR') }}
          </td>
          <td class="px-3 py-2">
            <button
              type="button"
              :disabled="busyId === user.id"
              class="rounded-md border border-border px-2 py-0.5 text-xs text-ink-muted hover:text-pepper hover:border-pepper disabled:opacity-50"
              @click="onPointAdjust(user, 100)"
            >
              +100p
            </button>
            <button
              type="button"
              :disabled="busyId === user.id"
              class="ml-1 rounded-md border border-border px-2 py-0.5 text-xs text-ink-muted hover:text-cheek hover:border-cheek disabled:opacity-50"
              @click="onPointAdjust(user, -100)"
            >
              -100p
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
