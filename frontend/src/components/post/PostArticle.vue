<script setup lang="ts">
defineProps<{
  title: string;
  author: string;
  createdAt: string;
  viewCount: number;
  content: string;
  likedByMe: boolean;
  likeCount: number;
}>();

const emit = defineEmits<{
  like: [];
}>();
</script>

<template>
  <article class="rounded-md bg-surface overflow-hidden">
    <slot name="media" />
    <div class="p-6">
      <h1 class="text-xl font-bold text-ink">{{ title }}</h1>
      <div class="mt-2 flex items-center gap-3 text-xs text-ink-muted">
        <span>{{ author }}</span>
        <span>·</span>
        <span>{{ new Date(createdAt).toLocaleDateString('ko-KR') }}</span>
        <span>·</span>
        <span>조회 {{ viewCount }}</span>
      </div>
      <p v-if="content" class="mt-4 whitespace-pre-wrap text-ink leading-relaxed">{{ content }}</p>
      <div class="mt-4 flex gap-2">
        <button
          type="button"
          class="rounded-md border border-border px-3 py-1.5 text-sm transition-colors"
          :class="likedByMe ? 'bg-cheek text-paper border-cheek' : 'text-ink-muted hover:text-cheek hover:border-cheek'"
          @click="emit('like')"
        >
          ♥ {{ likeCount }}
        </button>
      </div>
    </div>
  </article>
</template>
