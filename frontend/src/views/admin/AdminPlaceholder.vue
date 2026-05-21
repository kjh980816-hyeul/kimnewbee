<script setup lang="ts">
defineProps<{
  title: string;
  emoji: string;
  description: string;
  features: string[];
}>();
</script>

<template>
  <div class="p-8">
    <header class="mb-6">
      <h1 class="text-4xl font-extrabold text-ink leading-tight">{{ title }} {{ emoji }}</h1>
      <p class="mt-2 text-sm text-ink-muted">{{ description }}</p>
    </header>

    <div class="rounded-2xl bg-gradient-to-br from-violet-deep/30 via-surface to-surface border border-violet/20 p-10 text-center">
      <div class="text-5xl mb-4 opacity-60">{{ emoji }}</div>
      <h2 class="text-xl font-bold text-ink mb-2">곧 만나요!</h2>
      <p class="text-sm text-ink-muted mb-6">이 페이지는 준비 중이에요. 백엔드 API 작업 후 활성화됩니다.</p>

      <div class="max-w-md mx-auto text-left">
        <div class="text-xs font-semibold text-violet mb-2">예정된 기능</div>
        <ul class="space-y-1.5 text-xs text-ink-muted">
          <li v-for="f in features" :key="f" class="flex items-start gap-2">
            <span class="text-violet shrink-0">·</span>
            <span>{{ f }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
