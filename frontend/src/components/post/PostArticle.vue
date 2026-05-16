<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  title: string;
  author: string;
  authorTier?: string | null;
  authorLevel?: number | null;
  authorAvatar?: string | null;
  category?: string;
  createdAt: string;
  viewCount: number;
  content: string;
  likedByMe: boolean;
  likeCount: number;
}>();

defineEmits<{
  like: [];
}>();

interface TierMeta {
  label: string;
  badgeClass: string;
}

const tierMap: Record<string, TierMeta> = {
  seed: { label: '새싹', badgeClass: 'bg-pepper/20 text-pepper' },
  pepper: { label: '고추', badgeClass: 'bg-violet-deep/30 text-violet' },
  corn: { label: '옥수수', badgeClass: 'bg-corn/20 text-corn' },
  owner: { label: '발주인', badgeClass: 'bg-cheek/20 text-cheek' },
};

const tier = computed<TierMeta>(() => {
  const key = props.authorTier ?? 'seed';
  return tierMap[key] ?? tierMap['seed']!;
});

const formattedDate = computed(() => {
  const d = new Date(props.createdAt);
  const yy = String(d.getFullYear()).slice(2);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const mi = String(d.getMinutes()).padStart(2, '0');
  return `20${yy}.${mm}.${dd} · ${hh}:${mi}`;
});
</script>

<template>
  <article>
    <div v-if="category" class="mb-4 inline-block">
      <span class="px-4 py-1.5 rounded-full bg-violet text-ink text-sm font-semibold">
        {{ category }}
      </span>
    </div>

    <h1 class="text-3xl font-extrabold text-ink leading-tight mb-5">{{ title }}</h1>

    <div class="flex items-center gap-3 pb-5 mb-6 border-b border-border">
      <div class="w-11 h-11 rounded-full bg-gradient-to-br from-violet to-cheek flex items-center justify-center text-lg overflow-hidden shrink-0">
        <img v-if="authorAvatar" :src="authorAvatar" :alt="author" class="w-full h-full object-cover" />
        <span v-else>🌶️</span>
      </div>
      <div class="min-w-0">
        <div class="flex items-center gap-2 flex-wrap">
          <span class="text-sm font-semibold text-ink">{{ author }}</span>
          <span class="text-[10px] px-1.5 py-0.5 rounded font-semibold" :class="tier.badgeClass">
            {{ tier.label }}
          </span>
          <span v-if="authorLevel" class="text-[10px] text-ink-muted">lv.{{ authorLevel }}</span>
        </div>
        <div class="mt-0.5 text-xs text-ink-muted">
          {{ formattedDate }} · 조회 {{ viewCount }}
        </div>
      </div>
    </div>

    <slot name="media" />

    <div v-if="content" class="whitespace-pre-wrap text-ink leading-relaxed">{{ content }}</div>

    <div class="mt-8 flex justify-center">
      <button
        type="button"
        class="rounded-full px-5 py-2 text-sm font-semibold transition-colors"
        :class="likedByMe ? 'bg-cheek text-paper' : 'bg-elevated text-ink-muted hover:text-cheek border border-border hover:border-cheek'"
        @click="$emit('like')"
      >
        ♥ {{ likeCount }}
      </button>
    </div>
  </article>
</template>
