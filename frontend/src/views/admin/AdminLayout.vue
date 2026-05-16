<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter, RouterLink, RouterView, useRoute } from 'vue-router';
import { fetchAdminViewerInfo } from '@/api/letter';

const router = useRouter();
const route = useRoute();

const checking = ref(true);
const allowed = ref(false);

interface AdminMenu {
  to: { name: string };
  label: string;
  icon: string;
  enabled: boolean;
}

const menu: AdminMenu[] = [
  { to: { name: 'admin-dashboard' }, label: '대시보드', icon: '⌂', enabled: true },
  { to: { name: 'admin-boards' }, label: '게시판 관리', icon: '☰', enabled: true },
  { to: { name: 'admin-members' }, label: '회원 관리', icon: '👥', enabled: true },
  { to: { name: 'admin-dashboard' }, label: '등급·권한', icon: '☆', enabled: false },
  { to: { name: 'admin-dashboard' }, label: '공지사항', icon: '★', enabled: false },
  { to: { name: 'admin-cafe' }, label: '카페 꾸미기', icon: '🌶', enabled: true },
  { to: { name: 'admin-dashboard' }, label: '통계', icon: '👁', enabled: false },
];

function isActive(item: AdminMenu): boolean {
  return item.enabled && route.name === item.to.name;
}

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
  <p v-if="checking" class="p-8 text-ink-muted">권한 확인 중...</p>

  <div v-else-if="allowed" class="min-h-screen flex bg-paper text-ink">
    <aside class="w-60 shrink-0 bg-surface border-r border-border flex flex-col">
      <div class="px-6 pt-6 pb-2">
        <div class="text-[11px] tracking-widest text-cheek font-semibold mb-2">ADMIN</div>
        <h1 class="text-xl font-extrabold text-ink leading-tight">
          <span class="text-pepper">🌶</span> 고추밭 관리자
        </h1>
        <div class="mt-1 text-xs text-corn">👑 발주인 (김늉비)</div>
      </div>

      <nav class="flex-1 px-3 py-5 overflow-y-auto">
        <ul class="space-y-1">
          <li v-for="(item, i) in menu" :key="i">
            <RouterLink
              v-if="item.enabled"
              :to="item.to"
              class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors"
              :class="
                isActive(item)
                  ? 'bg-violet-deep/40 text-ink font-semibold'
                  : 'text-ink-muted hover:text-ink hover:bg-elevated'
              "
            >
              <span class="w-5 text-center text-base">{{ item.icon }}</span>
              <span class="flex-1">{{ item.label }}</span>
            </RouterLink>
            <div
              v-else
              class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-ink-muted/40 cursor-not-allowed"
            >
              <span class="w-5 text-center text-base">{{ item.icon }}</span>
              <span class="flex-1">{{ item.label }}</span>
            </div>
          </li>
        </ul>
      </nav>

      <div class="m-4 p-3 rounded-xl bg-corn/10 border border-corn/30 text-xs">
        <div class="font-semibold text-corn mb-1">🌽 도움말</div>
        <p class="text-ink-muted leading-relaxed">
          게시판마다 글 형식을 다르게 설정할 수 있어요
        </p>
      </div>
    </aside>

    <main class="flex-1 min-w-0">
      <RouterView />
    </main>
  </div>
</template>
