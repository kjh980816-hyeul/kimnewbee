<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import {
  createNotice,
  deleteNotice,
  fetchNotice,
  fetchNotices,
  updateNotice,
} from '@/api/notice';
import type { Notice, NoticeListItem } from '@/types/notice';

const notices = ref<NoticeListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const busyId = ref<number | null>(null);

const showModal = ref(false);
const editingId = ref<number | null>(null);
const form = reactive({ title: '', content: '' });
const submitting = ref(false);

const isEdit = computed(() => editingId.value !== null);
const canSubmit = computed(() => form.title.trim().length > 0 && form.content.trim().length > 0);

onMounted(load);

async function load(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    const res = await fetchNotices();
    notices.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '공지 목록을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
}

function openCreate(): void {
  editingId.value = null;
  form.title = '';
  form.content = '';
  showModal.value = true;
}

async function openEdit(item: NoticeListItem): Promise<void> {
  busyId.value = item.id;
  try {
    const full = await fetchNotice(item.id);
    editingId.value = item.id;
    form.title = full.title;
    form.content = full.content;
    showModal.value = true;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '공지 불러오기 실패';
  } finally {
    busyId.value = null;
  }
}

async function onSubmit(): Promise<void> {
  if (!canSubmit.value) return;
  submitting.value = true;
  try {
    const payload = { title: form.title.trim(), content: form.content.trim() };
    if (editingId.value !== null) {
      await updateNotice(editingId.value, payload);
    } else {
      await createNotice(payload);
    }
    showModal.value = false;
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '저장 실패';
  } finally {
    submitting.value = false;
  }
}

async function onDelete(item: NoticeListItem): Promise<void> {
  if (!confirm(`'${item.title}' 공지를 삭제할까요?`)) return;
  busyId.value = item.id;
  try {
    await deleteNotice(item.id);
    notices.value = notices.value.filter((n) => n.id !== item.id);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '삭제 실패';
  } finally {
    busyId.value = null;
  }
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString('ko-KR', {
    year: '2-digit', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
  });
}
</script>

<template>
  <div class="p-8">
    <header class="mb-6 flex items-end justify-between gap-4 flex-wrap">
      <div>
        <h1 class="text-4xl font-extrabold text-ink leading-tight font-serif">공지사항 📢</h1>
        <p class="mt-2 text-sm text-ink-muted">밭주인이 직접 작성하는 공지를 관리해요</p>
      </div>
      <button
        type="button"
        class="rounded-full bg-violet-deep px-5 py-2 text-sm font-semibold text-ink hover:bg-violet-deep/80 transition-colors"
        @click="openCreate"
      >
        + 새 공지 작성
      </button>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek mb-3">{{ error }}</p>

    <div v-else class="glass overflow-hidden">
      <div class="grid grid-cols-[40px_1fr_120px_80px_120px] gap-3 px-5 py-3 text-[11px] text-ink-muted border-b border-border tracking-wide">
        <span class="text-right">#</span>
        <span>제목</span>
        <span>작성자</span>
        <span class="text-right">조회</span>
        <span class="text-right">작성일</span>
      </div>
      <ul class="divide-y divide-border">
        <li
          v-for="notice in notices"
          :key="notice.id"
          class="grid grid-cols-[40px_1fr_120px_80px_120px] gap-3 px-5 py-3 items-center text-sm hover:bg-elevated transition-colors group"
        >
          <span class="text-xs text-ink-muted tabular-nums text-right">{{ notice.id }}</span>
          <div class="min-w-0">
            <button
              type="button"
              class="font-semibold text-ink truncate text-left hover:text-violet w-full"
              :disabled="busyId === notice.id"
              @click="openEdit(notice)"
            >
              {{ notice.title }}
            </button>
            <div class="flex items-center gap-2 mt-1 opacity-0 group-hover:opacity-100 transition-opacity">
              <button
                type="button"
                :disabled="busyId === notice.id"
                class="text-[10px] text-ink-muted hover:text-violet"
                @click="openEdit(notice)"
              >수정</button>
              <button
                type="button"
                :disabled="busyId === notice.id"
                class="text-[10px] text-ink-muted hover:text-cheek"
                @click="onDelete(notice)"
              >삭제</button>
            </div>
          </div>
          <span class="text-xs text-ink-muted truncate">{{ notice.author }}</span>
          <span class="text-xs text-ink-muted tabular-nums text-right">{{ notice.viewCount.toLocaleString() }}</span>
          <span class="text-xs text-ink-muted text-right">{{ formatDate(notice.createdAt) }}</span>
        </li>
      </ul>

      <p v-if="notices.length === 0" class="px-5 py-8 text-center text-ink-muted text-sm">
        아직 작성된 공지가 없어요
      </p>
    </div>

    <div
      v-if="showModal"
      class="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4"
      @click.self="showModal = false"
    >
      <div class="glass w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <header class="px-6 py-4 border-b border-border flex items-center justify-between">
          <div>
            <h2 class="text-2xl font-extrabold text-ink">{{ isEdit ? '공지 수정 ✏️' : '새 공지 작성 ✨' }}</h2>
            <p class="text-xs text-ink-muted mt-1">제목과 본문을 입력해주세요</p>
          </div>
          <button type="button" class="text-ink-muted hover:text-ink" @click="showModal = false">✕</button>
        </header>

        <div class="p-6 space-y-4">
          <label class="block">
            <span class="block text-xs text-ink-muted mb-1">제목 (최대 200자)</span>
            <input
              v-model="form.title"
              type="text"
              maxlength="200"
              placeholder="예: 5월 정기 점검 안내"
              class="w-full field-input"
            />
          </label>
          <label class="block">
            <span class="block text-xs text-ink-muted mb-1">본문</span>
            <textarea
              v-model="form.content"
              rows="12"
              placeholder="공지 내용을 작성해주세요. 줄바꿈은 그대로 표시됩니다."
              class="w-full field-input resize-y"
            ></textarea>
          </label>
        </div>

        <footer class="px-6 py-4 border-t border-border flex items-center justify-end gap-2">
          <button
            type="button"
            class="rounded-full border border-border px-5 py-2 text-sm text-ink-muted hover:text-ink"
            @click="showModal = false"
          >취소</button>
          <button
            type="button"
            :disabled="!canSubmit || submitting"
            class="rounded-full bg-violet-deep px-5 py-2 text-sm font-semibold text-ink hover:bg-violet-deep/80 disabled:opacity-50"
            @click="onSubmit"
          >
            {{ submitting ? '저장 중...' : (isEdit ? '수정 완료' : '게시') }}
          </button>
        </footer>
      </div>
    </div>
  </div>
</template>
