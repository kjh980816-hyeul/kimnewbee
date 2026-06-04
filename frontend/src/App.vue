<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { fetchCafeConfig } from '@/api/cafe';
import AppLayout from '@/layouts/AppLayout.vue';
import LoadingOverlay from '@/layouts/components/LoadingOverlay.vue';

const route = useRoute();
const footerText = ref<string | null>(null);

const useAppLayout = computed(() => {
  const path = route.path;
  if (path.startsWith('/admin')) return false;
  if (path === '/login') return false;
  return true;
});

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
  <LoadingOverlay />
  <AppLayout v-if="useAppLayout">
    <RouterView />
    <footer
      v-if="footerText"
      class="py-6 px-8 border-t border-border text-center text-xs text-ink-muted"
    >
      {{ footerText }}
    </footer>
  </AppLayout>
  <template v-else>
    <RouterView />
    <footer
      v-if="footerText"
      class="py-6 px-8 border-t border-border text-center text-xs text-ink-muted bg-paper"
    >
      {{ footerText }}
    </footer>
  </template>
</template>
