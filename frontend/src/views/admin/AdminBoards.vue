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

const LAYOUT_META: Record<BoardLayout, { label: string; emoji: string; desc: string; example: string }> = {
  list: { label: '리스트형', emoji: '📋', desc: '제목 중심의 일반 게시판', example: '예: 자유게시판, 공지' },
  gallery: { label: '갤러리형', emoji: '🖼', desc: '썸네일 그리드, 사진 위주', example: '예: 팬아트, 반려동물' },
  card: { label: '카드형', emoji: '🎴', desc: '썸네일 + 요약, SNS 느낌', example: '예: 후기' },
  video: { label: '영상형', emoji: '🎬', desc: '영상 임베드 우선, 클립 위주', example: '예: 클립, 영상 하이라이트' },
  letter: { label: '편지형', emoji: '✉', desc: '편지지 스타일, 감성 글', example: '예: 팬레터' },
  rank: { label: '투표형', emoji: '🏆', desc: '순위/투표 중심', example: '예: 노래추천, 회의 안건' },
};

const TIER_META: Record<Tier, { label: string; emoji: string }> = {
  seed: { label: '새싹', emoji: '🌱' },
  pepper: { label: '고추', emoji: '🌶' },
  corn: { label: '옥수수', emoji: '🌽' },
  owner: { label: '발주인', emoji: '👑' },
};

const showCreateModal = ref(false);
const newBoard = reactive({
  slug: '',
  name: '',
  description: '',
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

function openCreate(): void {
  Object.assign(newBoard, { slug: '', name: '', description: '', layout: 'LIST', readTier: 'SEED', writeTier: 'PEPPER' });
  showCreateModal.value = true;
}

async function onCreate(): Promise<void> {
  if (!canCreate.value) return;
  creating.value = true;
  try {
    await createBoard({
      slug: newBoard.slug,
      name: newBoard.name,
      layout: newBoard.layout,
      readTier: newBoard.readTier,
      writeTier: newBoard.writeTier,
    });
    showCreateModal.value = false;
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시판 추가에 실패했어요';
  } finally {
    creating.value = false;
  }
}

async function onToggleActive(board: Board): Promise<void> {
  busyId.value = board.id;
  try {
    const updated = await updateBoard(board.id, {
      name: board.name,
      layout: board.layout.toUpperCase() as BoardLayoutInput,
      readTier: board.readTier.toUpperCase() as TierInput,
      writeTier: board.writeTier.toUpperCase() as TierInput,
      active: !board.active,
    });
    Object.assign(board, updated);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시판 수정에 실패했어요';
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

async function onDelete(board: Board): Promise<void> {
  if (!confirm(`'${board.name}' 게시판을 삭제할까요?`)) return;
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
</script>

<template>
  <div class="p-8">
    <header class="mb-6 flex items-end justify-between gap-4 flex-wrap">
      <div>
        <h1 class="text-4xl font-extrabold text-ink leading-tight">게시판 관리 📁</h1>
        <p class="mt-2 text-sm text-ink-muted">드래그로 순서 변경 · 게시판마다 표시 형식과 권한을 지정할 수 있어요</p>
      </div>
      <div class="flex items-center gap-2">
        <button
          type="button"
          class="rounded-full border border-border px-4 py-2 text-sm text-ink-muted hover:text-ink hover:border-violet/40 transition-colors"
        >
          그룹 추가
        </button>
        <button
          type="button"
          class="rounded-full bg-violet-deep px-5 py-2 text-sm font-semibold text-ink hover:bg-violet-deep/80 transition-colors"
          @click="openCreate"
        >
          + 새 게시판
        </button>
      </div>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek mb-3">{{ error }}</p>

    <div v-else class="rounded-2xl bg-surface border border-border overflow-hidden mb-8">
      <div class="grid grid-cols-[24px_50px_1fr_120px_100px_110px_90px] gap-3 px-5 py-3 text-[11px] text-ink-muted border-b border-border tracking-wide">
        <span></span>
        <span>순서</span>
        <span>게시판 이름</span>
        <span>표시 형식</span>
        <span>쓰기 권한</span>
        <span>읽기 권한</span>
        <span class="text-right">상태</span>
      </div>
      <ul class="divide-y divide-border">
        <li
          v-for="(board, idx) in boards"
          :key="board.id"
          class="grid grid-cols-[24px_50px_1fr_120px_100px_110px_90px] gap-3 px-5 py-3 items-center text-sm hover:bg-elevated transition-colors"
        >
          <div class="flex flex-col gap-0.5 text-ink-muted">
            <button
              type="button"
              :disabled="idx === 0 || busyId === board.id"
              class="hover:text-ink disabled:opacity-30 leading-none text-xs"
              @click="move(board, -1)"
            >▲</button>
            <button
              type="button"
              :disabled="idx === boards.length - 1 || busyId === board.id"
              class="hover:text-ink disabled:opacity-30 leading-none text-xs"
              @click="move(board, 1)"
            >▼</button>
          </div>
          <span class="text-xs text-ink-muted tabular-nums">{{ idx + 1 }}</span>
          <div class="min-w-0">
            <div class="font-semibold text-ink truncate">{{ board.name }}</div>
            <div class="text-[10px] text-ink-muted font-mono truncate">/{{ board.slug }}</div>
          </div>
          <span class="text-[11px] px-2 py-0.5 rounded bg-violet/20 text-violet inline-flex items-center gap-1 w-max">
            {{ LAYOUT_META[board.layout].emoji }} {{ LAYOUT_META[board.layout].label }}
          </span>
          <span class="text-xs text-ink-muted">
            {{ TIER_META[board.writeTier].emoji }} {{ TIER_META[board.writeTier].label }}
          </span>
          <span class="text-xs text-ink-muted">
            {{ TIER_META[board.readTier].emoji }} {{ TIER_META[board.readTier].label }}
          </span>
          <div class="flex items-center justify-end gap-2">
            <button
              type="button"
              :disabled="busyId === board.id"
              class="text-[11px] px-2 py-0.5 rounded font-semibold transition-colors"
              :class="board.active ? 'bg-pepper/20 text-pepper hover:bg-pepper/30' : 'bg-elevated text-ink-muted hover:text-ink'"
              @click="onToggleActive(board)"
            >
              {{ board.active ? '공개' : '숨김' }}
            </button>
            <button
              type="button"
              :disabled="busyId === board.id"
              class="text-[11px] text-ink-muted hover:text-cheek transition-colors disabled:opacity-50"
              @click="onDelete(board)"
            >
              삭제
            </button>
          </div>
        </li>
      </ul>
    </div>

    <section>
      <h2 class="text-sm font-bold text-ink mb-3">지원하는 게시판 형식</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
        <div
          v-for="layout in layouts"
          :key="layout"
          class="rounded-xl bg-surface border border-border p-4"
        >
          <div class="flex items-center gap-2 mb-1">
            <span class="text-lg">{{ LAYOUT_META[layout].emoji }}</span>
            <span class="font-bold text-ink text-sm">{{ LAYOUT_META[layout].label }}</span>
          </div>
          <div class="text-xs text-ink-muted leading-snug">{{ LAYOUT_META[layout].desc }}</div>
          <div class="mt-2 text-[10px] text-ink-muted/70">{{ LAYOUT_META[layout].example }}</div>
        </div>
      </div>
    </section>

    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4"
      @click.self="showCreateModal = false"
    >
      <div class="rounded-2xl bg-surface border border-border w-full max-w-3xl max-h-[90vh] overflow-y-auto">
        <header class="px-6 py-4 border-b border-border flex items-center justify-between">
          <div>
            <h2 class="text-2xl font-extrabold text-ink">게시판 만들기 ✨</h2>
            <p class="text-xs text-ink-muted mt-1">이름과 형식, 권한을 정해주세요</p>
          </div>
          <button
            type="button"
            class="text-ink-muted hover:text-ink"
            @click="showCreateModal = false"
          >✕</button>
        </header>

        <div class="grid grid-cols-1 lg:grid-cols-[1fr_280px] gap-5 p-6">
          <div class="space-y-5">
            <div>
              <h3 class="text-sm font-bold text-ink mb-2">📋 기본 정보</h3>
              <label class="block mb-2">
                <span class="block text-xs text-ink-muted mb-1">게시판 이름</span>
                <input
                  v-model="newBoard.name"
                  type="text"
                  placeholder="예: 늉비 일상샷"
                  class="w-full rounded-lg bg-elevated border border-border px-3 py-2 text-sm text-ink"
                />
              </label>
              <label class="block mb-2">
                <span class="block text-xs text-ink-muted mb-1">slug (URL용, 소문자/숫자/하이픈)</span>
                <input
                  v-model="newBoard.slug"
                  type="text"
                  placeholder="my-board"
                  class="w-full rounded-lg bg-elevated border border-border px-3 py-2 text-sm text-ink font-mono"
                />
              </label>
              <label class="block">
                <span class="block text-xs text-ink-muted mb-1">설명 (부제, 선택)</span>
                <input
                  v-model="newBoard.description"
                  type="text"
                  placeholder="늉비가 올려주는 일상 사진들~"
                  class="w-full rounded-lg bg-elevated border border-border px-3 py-2 text-sm text-ink"
                />
              </label>
            </div>

            <div>
              <h3 class="text-sm font-bold text-ink mb-2">🎨 표시 형식</h3>
              <p class="text-[11px] text-ink-muted mb-2">회원들이 이 게시판에 들어왔을 때 보이는 레이아웃</p>
              <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
                <button
                  v-for="layout in layouts"
                  :key="layout"
                  type="button"
                  class="rounded-lg border p-3 text-left transition-all"
                  :class="
                    newBoard.layout === layout.toUpperCase()
                      ? 'bg-violet/20 border-violet/50'
                      : 'bg-elevated border-border hover:border-violet/30'
                  "
                  @click="newBoard.layout = layout.toUpperCase() as BoardLayoutInput"
                >
                  <div class="flex items-center gap-1.5 mb-1">
                    <span class="text-base">{{ LAYOUT_META[layout].emoji }}</span>
                    <span class="text-xs font-bold text-ink">{{ LAYOUT_META[layout].label }}</span>
                    <span v-if="newBoard.layout === layout.toUpperCase()" class="text-violet text-xs">✓</span>
                  </div>
                  <div class="text-[10px] text-ink-muted leading-snug">{{ LAYOUT_META[layout].desc }}</div>
                </button>
              </div>
            </div>
          </div>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-bold text-ink mb-2">🔒 권한 설정</h3>
              <label class="block mb-2">
                <span class="block text-xs text-ink-muted mb-1">읽기</span>
                <select v-model="newBoard.readTier" class="w-full rounded-lg bg-elevated border border-border px-3 py-2 text-sm text-ink">
                  <option v-for="t in tiers" :key="t" :value="t.toUpperCase()">
                    {{ TIER_META[t].emoji }} {{ TIER_META[t].label }} 이상
                  </option>
                </select>
              </label>
              <label class="block">
                <span class="block text-xs text-ink-muted mb-1">글쓰기</span>
                <select v-model="newBoard.writeTier" class="w-full rounded-lg bg-elevated border border-border px-3 py-2 text-sm text-ink">
                  <option v-for="t in tiers" :key="t" :value="t.toUpperCase()">
                    {{ TIER_META[t].emoji }} {{ TIER_META[t].label }} 이상
                  </option>
                </select>
              </label>
            </div>

            <div class="rounded-xl bg-elevated p-3">
              <h4 class="text-xs font-bold text-ink mb-2">📌 미리보기</h4>
              <div class="text-xs text-ink mb-1 truncate">{{ newBoard.name || '게시판 이름' }}</div>
              <div class="text-[10px] text-ink-muted mb-2">{{ newBoard.description || '설명 없음' }}</div>
              <div class="grid grid-cols-3 gap-1">
                <div v-for="i in 6" :key="i" class="aspect-square rounded bg-paper/50 border border-border"></div>
              </div>
            </div>
          </div>
        </div>

        <footer class="px-6 py-4 border-t border-border flex items-center justify-end gap-2">
          <button
            type="button"
            class="rounded-full border border-border px-5 py-2 text-sm text-ink-muted hover:text-ink"
            @click="showCreateModal = false"
          >취소</button>
          <button
            type="button"
            :disabled="!canCreate || creating"
            class="rounded-full bg-violet-deep px-5 py-2 text-sm font-semibold text-ink hover:bg-violet-deep/80 disabled:opacity-50"
            @click="onCreate"
          >
            {{ creating ? '만드는 중...' : '게시판 만들기' }}
          </button>
        </footer>
      </div>
    </div>
  </div>
</template>
