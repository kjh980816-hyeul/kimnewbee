<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchFanart, toggleFanartLike } from '@/api/fanart';
import { useIsOwner } from '@/composables/useIsOwner';
import type { Fanart } from '@/types/fanart';
import PostArticle from '@/components/post/PostArticle.vue';
import CommentSection from '@/components/post/CommentSection.vue';

const route = useRoute();
const router = useRouter();

const fanart = ref<Fanart | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const fanartId = computed(() => Number(route.params['id']));
const isOwner = useIsOwner(computed(() => fanart.value?.author ?? null));

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
      <div v-if="isOwner" class="mb-3 flex justify-end">
        <RouterLink
          :to="{ name: 'fanart-edit', params: { id: fanart.id } }"
          class="rounded-md border border-border px-3 py-1.5 text-sm text-ink-muted hover:text-pepper hover:border-pepper transition-colors"
        >
          수정
        </RouterLink>
      </div>
      <PostArticle
        :title="fanart.title"
        :author="fanart.author"
        :created-at="fanart.createdAt"
        :view-count="fanart.viewCount"
        :content="fanart.content"
        :liked-by-me="fanart.likedByMe"
        :like-count="fanart.likeCount"
        @like="onLike"
      >
        <template #media>
          <img
            :src="fanart.imageUrl"
            :alt="fanart.title"
            class="w-full max-h-[80vh] object-contain bg-elevated"
          />
        </template>
      </PostArticle>
      <CommentSection :post-id="fanart.id" class="mt-6" />
    </template>
  </main>
</template>
