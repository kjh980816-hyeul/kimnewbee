<script setup lang="ts">
// 데이터 로딩 중 콘텐츠 모양의 회색 shimmer placeholder.
withDefaults(
  defineProps<{
    variant?: 'rows' | 'gallery' | 'cards';
    count?: number;
  }>(),
  { variant: 'rows', count: 4 },
);
</script>

<template>
  <!-- 리스트 행 -->
  <div v-if="variant === 'rows'" class="space-y-3 py-1">
    <div v-for="i in count" :key="i" class="flex items-center gap-3">
      <div class="skeleton h-6 w-12 shrink-0"></div>
      <div class="flex-1 space-y-1.5">
        <div class="skeleton h-3.5" :style="{ width: `${70 - (i % 3) * 12}%` }"></div>
        <div class="skeleton h-2.5 w-1/3"></div>
      </div>
    </div>
  </div>

  <!-- 갤러리 썸네일 -->
  <div v-else-if="variant === 'gallery'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
    <div v-for="i in count" :key="i" class="space-y-2">
      <div class="skeleton aspect-square rounded-2xl"></div>
      <div class="skeleton h-3.5 w-3/4"></div>
      <div class="skeleton h-2.5 w-1/2"></div>
    </div>
  </div>

  <!-- 카드 -->
  <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <div v-for="i in count" :key="i" class="glass card-pad space-y-3">
      <div class="skeleton h-4 w-1/3"></div>
      <div class="skeleton h-3" :style="{ width: `${85 - (i % 2) * 15}%` }"></div>
      <div class="skeleton h-3 w-2/3"></div>
    </div>
  </div>
</template>
