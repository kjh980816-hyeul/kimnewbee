<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchOfflineReviews } from '@/api/offline';
import { isHttpStatus } from '@/api/error';
import type { OfflineReviewListItem } from '@/types/offline';

const reviews = ref<OfflineReviewListItem[]>([]);
const loading = ref(true);
const accessDenied = ref(false);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const res = await fetchOfflineReviews();
    reviews.value = res.data;
  } catch (e) {
    if (isHttpStatus(e, 403)) {
      accessDenied.value = true;
    } else {
      error.value = e instanceof Error ? e.message : '후기를 불러올 수 없어요';
    }
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <header class="mb-6 flex items-end justify-between">
      <div>
        <h1 class="text-2xl font-bold text-pepper">오프 후기</h1>
        <p class="mt-1 text-ink-muted text-sm">
          정모/번개 후기 — 🌽 옥수수 등급 이상 열람
        </p>
      </div>
      <RouterLink
        v-if="!accessDenied"
        :to="{ name: 'offline-write' }"
        class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep transition-colors"
      >
        후기 쓰기
      </RouterLink>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <div
      v-else-if="accessDenied"
      class="rounded-md bg-surface p-8 text-center"
    >
      <p class="text-2xl">🌽</p>
      <p class="mt-2 text-ink">옥수수 등급 이상만 볼 수 있어요</p>
      <p class="mt-1 text-sm text-ink-muted">
        고정/후원 팬에게만 공개되는 게시판입니다
      </p>
    </div>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <ul
      v-else
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"
    >
      <li
        v-for="review in reviews"
        :key="review.id"
        class="rounded-md bg-surface overflow-hidden hover:bg-elevated transition-colors"
      >
        <RouterLink :to="{ name: 'offline-detail', params: { id: review.id } }">
          <div class="aspect-video overflow-hidden bg-elevated">
            <img
              :src="review.thumbnailUrl"
              :alt="review.title"
              class="w-full h-full object-cover"
              loading="lazy"
            />
          </div>
          <div class="p-4">
            <h2 class="text-ink truncate font-medium">{{ review.title }}</h2>
            <div class="mt-1 text-xs text-ink-muted flex gap-2">
              <span>📍 {{ review.location }}</span>
              <span>·</span>
              <span>📅 {{ review.meetupDate }}</span>
            </div>
            <p class="mt-2 text-sm text-ink-muted line-clamp-2">{{ review.preview }}</p>
            <div class="mt-3 flex items-center justify-between text-xs text-ink-muted">
              <span class="truncate">{{ review.author }}</span>
              <span class="flex gap-2 whitespace-nowrap ml-2">
                <span>♥ {{ review.likeCount }}</span>
                <span v-if="review.commentCount > 0" class="text-corn">
                  💬 {{ review.commentCount }}
                </span>
              </span>
            </div>
          </div>
        </RouterLink>
      </li>
    </ul>
  </main>
</template>
