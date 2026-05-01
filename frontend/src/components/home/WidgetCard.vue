<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router';

defineProps<{
  title: string;
  viewAllRoute: RouteLocationRaw;
}>();
</script>

<template>
  <section class="rounded-md bg-surface p-4">
    <header class="mb-3 flex items-center justify-between">
      <h2 class="text-sm font-medium text-ink">{{ title }}</h2>
      <RouterLink
        :to="viewAllRoute"
        class="text-xs text-ink-muted hover:text-pepper transition-colors"
      >
        전체보기 →
      </RouterLink>
    </header>
    <slot />
  </section>
</template>
