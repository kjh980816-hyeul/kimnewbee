<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchFanart, toggleFanartLike } from '@/api/fanart';
import type { Fanart } from '@/types/fanart';
import CommentSection from '@/components/post/CommentSection.vue';

const route = useRoute();
const router = useRouter();

const fanart = ref<Fanart | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const fanartId = computed(() => Number(route.params['id']));

onMounted(async () => {
  if (Number.isNaN(fanartId.value)) {
    error.value = '잘못된 팬아트 번호예요';
    loading.value = false;
    return;
  }
  try {
    fanart.value = await fetchFanart(fanartId.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '팬아트를 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onLike(): Promise<void> {
  if (!fanart.value) return;
  const res = await toggleFanartLike(fanart.value.id);
  fanart.value.likedByMe = res.liked;
  fanart.value.likeCount = res.likeCount;
}
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <button
      type="button"
      class="mb-4 text-sm text-ink-muted hover:text-ink"
      @click="router.back()"
    >
      ← 갤러리로
    </button>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="fanart">
      <article class="rounded-md bg-surface overflow-hidden">
        <img
          :src="fanart.imageUrl"
          :alt="fanart.title"
          class="w-full max-h-[80vh] object-contain bg-elevated"
        />
        <div class="p-6">
          <h1 class="text-xl font-bold text-ink">{{ fanart.title }}</h1>
          <div class="mt-2 flex items-center gap-3 text-xs text-ink-muted">
            <span>{{ fanart.author }}</span>
            <span>·</span>
            <span>{{ new Date(fanart.createdAt).toLocaleDateString('ko-KR') }}</span>
            <span>·</span>
            <span>조회 {{ fanart.viewCount }}</span>
          </div>
          <p v-if="fanart.content" class="mt-4 whitespace-pre-wrap text-ink leading-relaxed">
            {{ fanart.content }}
          </p>
          <div class="mt-4 flex gap-2">
            <button
              type="button"
              class="rounded-md border border-border px-3 py-1.5 text-sm transition-colors"
              :class="fanart.likedByMe ? 'bg-cheek text-paper border-cheek' : 'text-ink-muted hover:text-cheek hover:border-cheek'"
              @click="onLike"
            >
              ♥ {{ fanart.likeCount }}
            </button>
          </div>
        </div>
      </article>

      <CommentSection :post-id="fanart.id" class="mt-6" />
    </template>
  </main>
</template>
