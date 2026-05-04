<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchCafeConfig } from '@/api/cafe';

const footerText = ref<string | null>(null);

onMounted(async () => {
  try {
    const cfg = await fetchCafeConfig();
    footerText.value = cfg.footerText;
  } catch {
    /* footer 비표시는 graceful degrade */
  }
});
</script>

<template>
  <RouterView />
  <footer
    v-if="footerText"
    class="py-6 px-8 border-t border-border text-center text-xs text-ink-muted bg-paper"
  >
    {{ footerText }}
  </footer>
</template>
