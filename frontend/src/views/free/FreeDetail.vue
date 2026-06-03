<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { fetchFreePost, toggleFreePostLike } from '@/api/free';
import { deletePostAsAdmin } from '@/api/admin';
import { useIsOwner } from '@/composables/useIsOwner';
import { useCurrentUser } from '@/composables/useCurrentUser';
import { errorMessage } from '@/api/error';
import type { FreePost } from '@/types/free';
import PostArticle from '@/components/post/PostArticle.vue';
import CommentSection from '@/components/post/CommentSection.vue';
import MarkdownContent from '@/components/MarkdownContent.vue';

const route = useRoute();
const router = useRouter();

const post = ref<FreePost | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const postId = computed(() => Number(route.params['id']));
const isOwner = useIsOwner(computed(() => post.value?.author ?? null));
const { isOwner: isAdmin } = useCurrentUser();

async function handleDelete(): Promise<void> {
  if (!post.value) return;
  if (!confirm('이 게시글을 삭제할까요? 되돌릴 수 없어요.')) return;
  try {
    await deletePostAsAdmin(post.value.id);
    await router.push({ name: 'free' });
  } catch (e) {
    error.value = errorMessage(e, '삭제에 실패했어요');
  }
}

onMounted(async () => {
  if (Number.isNaN(postId.value)) {
    error.value = '잘못된 게시글 번호예요';
    loading.value = false;
    return;
  }
  try {
    post.value = await fetchFreePost(postId.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시글을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onLike(): Promise<void> {
  if (!post.value) return;
  const res = await toggleFreePostLike(post.value.id);
  post.value.likedByMe = res.liked;
  post.value.likeCount = res.likeCount;
}
</script>

<template>
  <div class="p-8 max-w-3xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <RouterLink :to="{ name: 'free' }" class="hover:text-ink">자유게시판</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">잡담</span>
    </nav>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="post">
      <div v-if="isOwner" class="mb-3 flex justify-end">
        <RouterLink
          :to="{ name: 'free-edit', params: { id: post.id } }"
          class="rounded-md border border-border px-3 py-1.5 text-sm text-ink-muted hover:text-pepper hover:border-pepper transition-colors"
        >
          수정
        </RouterLink>
      </div>
      <PostArticle
        :title="post.title"
        :author="post.author"
        :author-avatar="post.authorProfileImage"
        :created-at="post.createdAt"
        :view-count="post.viewCount"
        :content="post.content"
        :liked-by-me="post.likedByMe"
        :like-count="post.likeCount"
        :can-delete="isAdmin"
        category="잡담"
        @like="onLike"
        @delete="handleDelete"
      >
        <template #body>
          <MarkdownContent :content="post.content" />
        </template>
      </PostArticle>
      <CommentSection :post-id="post.id" class="mt-10" />
    </template>
  </div>
</template>
