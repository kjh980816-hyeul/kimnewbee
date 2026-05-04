<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter, RouterLink, RouterView } from 'vue-router';
import { fetchAdminViewerInfo } from '@/api/letter';

const router = useRouter();

const checking = ref(true);
const allowed = ref(false);

onMounted(async () => {
  try {
    const info = await fetchAdminViewerInfo();
    if (info.isAdmin) {
      allowed.value = true;
    } else {
      await router.replace({ name: 'home' });
    }
  } catch {
    await router.replace({ name: 'home' });
  } finally {
    checking.value = false;
  }
});
</script>

<template>
  <main class="min-h-screen bg-paper text-ink">
    <p v-if="checking" class="p-8 text-ink-muted">권한 확인 중...</p>

    <template v-else-if="allowed">
      <header class="bg-surface border-b border-border px-6 py-3 flex items-center gap-6">
        <h1 class="text-lg font-bold text-pepper">고추밭 관리자</h1>
        <nav class="flex gap-4 text-sm">
          <RouterLink
            :to="{ name: 'admin-dashboard' }"
            class="text-ink-muted hover:text-pepper"
            active-class="text-pepper"
          >
            대시보드
          </RouterLink>
          <RouterLink
            :to="{ name: 'admin-members' }"
            class="text-ink-muted hover:text-pepper"
            active-class="text-pepper"
          >
            회원 관리
          </RouterLink>
        </nav>
      </header>

      <section class="p-6">
        <RouterView />
      </section>
    </template>
  </main>
</template>
