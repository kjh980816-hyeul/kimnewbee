<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { fetchBoardPost, toggleBoardPostLike } from '@/api/boardPost';
import { deletePost } from '@/api/post';
import { useBoards } from '@/composables/useBoards';
import { useCurrentUser } from '@/composables/useCurrentUser';
import { errorMessage } from '@/api/error';
import type { BoardPostDetail } from '@/types/boardPost';
import PostArticle from '@/components/post/PostArticle.vue';
import CommentSection from '@/components/post/CommentSection.vue';

const route = useRoute();
const router = useRouter();
const slug = computed(() => String(route.params['slug']));
const postId = computed(() => Number(route.params['id']));

const { boards } = useBoards();
const board = computed(() => boards.value.find((b) => b.slug === slug.value) ?? null);
const isVideo = computed(() => board.value?.layout === 'video');

const post = ref<BoardPostDetail | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const { currentUser, isOwner } = useCurrentUser();
const canDelete = computed(
  () => isOwner.value || (!!currentUser.value && currentUser.value.nickname === post.value?.author),
);

async function load(): Promise<void> {
  loading.value = true;
  error.value = null;
  if (Number.isNaN(postId.value)) {
    error.value = '잘못된 게시글 번호예요';
    loading.value = false;
    return;
  }
  try {
    post.value = await fetchBoardPost(slug.value, postId.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시글을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
}

onMounted(load);
watch([slug, postId], load);

async function onLike(): Promise<void> {
  if (!post.value) return;
  const res = await toggleBoardPostLike(slug.value, post.value.id);
  post.value.likedByMe = res.liked;
  post.value.likeCount = res.likeCount;
}

async function handleDelete(): Promise<void> {
  if (!post.value) return;
  if (!confirm('이 게시글을 삭제할까요? 되돌릴 수 없어요.')) return;
  try {
    await deletePost(post.value.id);
    await router.push({ name: 'board', params: { slug: slug.value } });
  } catch (e) {
    error.value = errorMessage(e, '삭제에 실패했어요');
  }
}
</script>

<template>
  <div class="p-8 max-w-3xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <RouterLink :to="{ name: 'board', params: { slug } }" class="hover:text-ink">{{ board?.name ?? '게시판' }}</RouterLink>
    </nav>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <PostArticle
      v-else-if="post"
      :title="post.title"
      :author="post.author"
      :author-avatar="post.authorProfileImage"
      :author-tier="post.authorTier"
      :created-at="post.createdAt"
      :view-count="post.viewCount"
      :content="post.content"
      :liked-by-me="post.likedByMe"
      :like-count="post.likeCount"
      :can-delete="canDelete"
      @like="onLike"
      @delete="handleDelete"
    >
      <template #media>
        <div v-if="post.mediaUrl" class="mb-6">
          <a
            v-if="isVideo"
            :href="post.mediaUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="block rounded-xl border border-border bg-elevated aspect-video flex items-center justify-center text-ink-muted hover:text-ink"
          >
            <span class="text-2xl mr-2">▶</span> 영상 보기
          </a>
          <img
            v-else
            :src="post.mediaUrl"
            :alt="post.title"
            class="w-full rounded-xl border border-border object-contain max-h-[600px]"
          />
        </div>
      </template>
    </PostArticle>

    <CommentSection v-if="post" :post-id="post.id" class="mt-10" />
  </div>
</template>
