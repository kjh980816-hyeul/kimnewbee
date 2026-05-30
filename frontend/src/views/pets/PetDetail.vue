<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { fetchPet, togglePetLike } from '@/api/pet';
import { useIsOwner } from '@/composables/useIsOwner';
import type { Pet } from '@/types/pet';
import PostArticle from '@/components/post/PostArticle.vue';
import CommentSection from '@/components/post/CommentSection.vue';

const route = useRoute();

const pet = ref<Pet | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const petId = computed(() => Number(route.params['id']));
const isOwner = useIsOwner(computed(() => pet.value?.author ?? null));

onMounted(async () => {
  if (Number.isNaN(petId.value)) {
    error.value = '잘못된 사진 번호예요';
    loading.value = false;
    return;
  }
  try {
    pet.value = await fetchPet(petId.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '사진을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onLike(): Promise<void> {
  if (!pet.value) return;
  const res = await togglePetLike(pet.value.id);
  pet.value.likedByMe = res.liked;
  pet.value.likeCount = res.likeCount;
}
</script>

<template>
  <div class="p-8 max-w-3xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <RouterLink :to="{ name: 'pets' }" class="hover:text-ink">반려동물 사진</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">반려동물</span>
    </nav>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="pet">
      <div v-if="isOwner" class="mb-3 flex justify-end">
        <RouterLink
          :to="{ name: 'pet-edit', params: { id: pet.id } }"
          class="rounded-md border border-border px-3 py-1.5 text-sm text-ink-muted hover:text-pepper hover:border-pepper transition-colors"
        >
          수정
        </RouterLink>
      </div>
      <PostArticle
        :title="pet.title"
        :author="pet.author"
        :author-avatar="pet.authorProfileImage"
        :created-at="pet.createdAt"
        :view-count="pet.viewCount"
        :content="pet.content"
        :liked-by-me="pet.likedByMe"
        :like-count="pet.likeCount"
        category="반려동물"
        @like="onLike"
      >
        <template #media>
          <img
            :src="pet.imageUrl"
            :alt="pet.title"
            class="w-full max-h-[80vh] object-contain bg-elevated rounded-xl mb-6"
          />
        </template>
      </PostArticle>
      <CommentSection :post-id="pet.id" class="mt-10" />
    </template>
  </div>
</template>
