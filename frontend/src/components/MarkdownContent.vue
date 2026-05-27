<script setup lang="ts">
import { computed } from 'vue';
import { renderImageMarkdown } from '@/utils/markdown';

const props = defineProps<{
  content: string;
}>();

const html = computed(() => renderImageMarkdown(props.content));
</script>

<template>
  <div class="text-ink leading-relaxed" v-html="html" />
</template>
