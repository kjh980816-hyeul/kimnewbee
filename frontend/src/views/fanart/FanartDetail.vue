<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { fetchFanart, toggleFanartLike } from '@/api/fanart';
import { useIsOwner } from '@/composables/useIsOwner';
import type { Fanart } from '@/types/fanart';
import PostArticle from '@/components/post/PostArticle.vue';
import CommentSection from '@/components/post/CommentSection.vue';

const route = useRoute();

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
  <div class="p-8 max-w-3xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <RouterLink :to="{ name: 'fanart' }" class="hover:text-ink">팬아트 갤러리</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">일러스트</span>
    </nav>

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
        :author-avatar="fanart.authorProfileImage"
        :created-at="fanart.createdAt"
        :view-count="fanart.viewCount"
        :content="fanart.content"
        :liked-by-me="fanart.likedByMe"
        :like-count="fanart.likeCount"
        category="일러스트"
        @like="onLike"
      >
        <template #media>
          <img
            :src="fanart.imageUrl"
            :alt="fanart.title"
            class="w-full max-h-[80vh] object-contain bg-elevated rounded-xl mb-6"
          />
        </template>
      </PostArticle>
      <CommentSection :post-id="fanart.id" class="mt-10" />
    </template>
  </div>
</template>
