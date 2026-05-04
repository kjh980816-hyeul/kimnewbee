<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import {
  createBoard,
  deleteBoard,
  fetchAdminBoards,
  reorderBoards,
  updateBoard,
} from '@/api/board';
import type { Board, BoardLayout, BoardLayoutInput, TierInput } from '@/types/board';
import type { Tier } from '@/types/offline';

const boards = ref<Board[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const busyId = ref<number | null>(null);

const layouts: BoardLayout[] = ['list', 'gallery', 'card', 'video', 'letter', 'rank'];
const tiers: Tier[] = ['seed', 'pepper', 'corn', 'owner'];

const newBoard = reactive({
  slug: '',
  name: '',
  layout: 'LIST' as BoardLayoutInput,
  readTier: 'SEED' as TierInput,
  writeTier: 'PEPPER' as TierInput,
});
const creating = ref(false);

const canCreate = computed(
  () => /^[a-z][a-z0-9-]{0,59}$/.test(newBoard.slug) && newBoard.name.trim().length > 0,
);

onMounted(load);

async function load(): Promise<void> {
  loading.value = true;
  try {
    const res = await fetchAdminBoards();
    boards.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시판 목록을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
}

async function onCreate(): Promise<void> {
  if (!canCreate.value) return;
  creating.value = true;
  try {
    await createBoard({ ...newBoard });
    Object.assign(newBoard, { slug: '', name: '', layout: 'LIST', readTier: 'SEED', writeTier: 'PEPPER' });
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시판 추가에 실패했어요';
  } finally {
    creating.value = false;
  }
}

async function onPersist(board: Board): Promise<void> {
  busyId.value = board.id;
  try {
    const updated = await updateBoard(board.id, {
      name: board.name,
      layout: board.layout.toUpperCase() as BoardLayoutInput,
      readTier: board.readTier.toUpperCase() as TierInput,
      writeTier: board.writeTier.toUpperCase() as TierInput,
      active: board.active,
    });
    Object.assign(board, updated);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시판 수정에 실패했어요';
  } finally {
    busyId.value = null;
  }
}

async function onDelete(board: Board): Promise<void> {
  busyId.value = board.id;
  try {
    await deleteBoard(board.id);
    boards.value = boards.value.filter((b) => b.id !== board.id);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시판 삭제에 실패했어요';
  } finally {
    busyId.value = null;
  }
}

async function move(board: Board, direction: -1 | 1): Promise<void> {
  const idx = boards.value.findIndex((b) => b.id === board.id);
  const swap = idx + direction;
  if (swap < 0 || swap >= boards.value.length) return;
  const reordered = [...boards.value];
  [reordered[idx], reordered[swap]] = [reordered[swap]!, reordered[idx]!];
  busyId.value = board.id;
  try {
    const res = await reorderBoards(reordered.map((b) => b.id));
    boards.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '순서 변경에 실패했어요';
  } finally {
    busyId.value = null;
  }
}
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold text-ink mb-4">게시판 관리</h2>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek mb-3">{{ error }}</p>

    <table v-if="!loading" class="w-full text-sm mb-6">
      <thead class="bg-surface text-ink-muted">
        <tr>
          <th class="px-3 py-2 text-left w-32">slug</th>
          <th class="px-3 py-2 text-left">이름</th>
          <th class="px-3 py-2 text-left w-28">레이아웃</th>
          <th class="px-3 py-2 text-left w-24">read</th>
          <th class="px-3 py-2 text-left w-24">write</th>
          <th class="px-3 py-2 text-left w-20">활성</th>
          <th class="px-3 py-2 text-left w-44">관리</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="board in boards" :key="board.id" class="border-b border-border">
          <td class="px-3 py-2 text-ink-muted font-mono">{{ board.slug }}</td>
          <td class="px-3 py-2">
            <input
              v-model="board.name"
              class="w-full rounded-md bg-elevated border border-border px-2 py-1 text-ink"
            />
          </td>
          <td class="px-3 py-2">
            <select
              v-model="board.layout"
              class="rounded-md bg-elevated border border-border px-2 py-1 text-ink"
            >
              <option v-for="l in layouts" :key="l" :value="l">{{ l }}</option>
            </select>
          </td>
          <td class="px-3 py-2">
            <select
              v-model="board.readTier"
              class="rounded-md bg-elevated border border-border px-2 py-1 text-ink"
            >
              <option v-for="t in tiers" :key="t" :value="t">{{ t }}</option>
            </select>
          </td>
          <td class="px-3 py-2">
            <select
              v-model="board.writeTier"
              class="rounded-md bg-elevated border border-border px-2 py-1 text-ink"
            >
              <option v-for="t in tiers" :key="t" :value="t">{{ t }}</option>
            </select>
          </td>
          <td class="px-3 py-2">
            <input v-model="board.active" type="checkbox" class="w-4 h-4" />
          </td>
          <td class="px-3 py-2 space-x-1">
            <button
              type="button"
              :disabled="busyId === board.id"
              class="rounded-md border border-border px-2 py-0.5 text-xs text-ink-muted hover:text-pepper hover:border-pepper disabled:opacity-50"
              @click="move(board, -1)"
            >
              ↑
            </button>
            <button
              type="button"
              :disabled="busyId === board.id"
              class="rounded-md border border-border px-2 py-0.5 text-xs text-ink-muted hover:text-pepper hover:border-pepper disabled:opacity-50"
              @click="move(board, 1)"
            >
              ↓
            </button>
            <button
              type="button"
              :disabled="busyId === board.id"
              class="rounded-md border border-border px-2 py-0.5 text-xs text-pepper hover:bg-pepper/10 disabled:opacity-50"
              @click="onPersist(board)"
            >
              저장
            </button>
            <button
              type="button"
              :disabled="busyId === board.id"
              class="rounded-md border border-border px-2 py-0.5 text-xs text-cheek hover:bg-cheek/10 disabled:opacity-50"
              @click="onDelete(board)"
            >
              삭제
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <section class="rounded-md bg-surface p-4 max-w-2xl">
      <h3 class="text-sm font-medium text-ink mb-3">게시판 추가</h3>
      <div class="grid grid-cols-2 gap-2 mb-3">
        <input
          v-model="newBoard.slug"
          placeholder="slug (소문자/숫자/하이픈)"
          class="rounded-md bg-elevated border border-border px-2 py-1 text-ink placeholder:text-ink-muted"
        />
        <input
          v-model="newBoard.name"
          placeholder="이름"
          class="rounded-md bg-elevated border border-border px-2 py-1 text-ink placeholder:text-ink-muted"
        />
        <select v-model="newBoard.layout" class="rounded-md bg-elevated border border-border px-2 py-1 text-ink">
          <option v-for="l in layouts" :key="l" :value="l.toUpperCase()">{{ l }}</option>
        </select>
        <select v-model="newBoard.readTier" class="rounded-md bg-elevated border border-border px-2 py-1 text-ink">
          <option v-for="t in tiers" :key="t" :value="t.toUpperCase()">read: {{ t }}</option>
        </select>
        <select v-model="newBoard.writeTier" class="rounded-md bg-elevated border border-border px-2 py-1 text-ink">
          <option v-for="t in tiers" :key="t" :value="t.toUpperCase()">write: {{ t }}</option>
        </select>
      </div>
      <button
        type="button"
        :disabled="!canCreate || creating"
        class="rounded-md bg-pepper px-3 py-1.5 text-sm font-medium text-paper hover:bg-pepper-deep disabled:opacity-50"
        @click="onCreate"
      >
        {{ creating ? '추가 중...' : '추가하기' }}
      </button>
    </section>
  </div>
</template>
