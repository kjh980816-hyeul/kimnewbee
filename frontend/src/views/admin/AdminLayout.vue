<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, RouterView, useRoute } from 'vue-router';
import { fetchAdminViewerInfo } from '@/api/letter';
import { isHttpStatus } from '@/api/error';

const route = useRoute();

type AccessState = 'checking' | 'allowed' | 'unauthorized' | 'forbidden';
const access = ref<AccessState>('checking');

onMounted(async () => {
  try {
    const info = await fetchAdminViewerInfo();
    access.value = info.isAdmin ? 'allowed' : 'forbidden';
  } catch (e) {
    if (isHttpStatus(e, 401)) {
      access.value = 'unauthorized';
    } else if (isHttpStatus(e, 403)) {
      access.value = 'forbidden';
    } else {
      access.value = 'unauthorized';
    }
  }
});

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
  { to: { name: 'admin-roles' }, label: '등급·권한', icon: '🏅', enabled: true },
  { to: { name: 'admin-notices' }, label: '공지사항', icon: '📢', enabled: true },
  { to: { name: 'admin-cafe' }, label: '카페 꾸미기', icon: '🎨', enabled: true },
  { to: { name: 'admin-stats' }, label: '통계', icon: '📈', enabled: true },
];

function isActive(item: AdminMenu): boolean {
  return item.enabled && route.name === item.to.name;
}
</script>

<template>
  <div v-if="access === 'checking'" class="min-h-screen grid place-items-center bg-paper text-ink-muted text-sm">
    권한 확인 중...
  </div>

  <div
    v-else-if="access === 'unauthorized'"
    class="min-h-screen grid place-items-center bg-paper text-ink p-8"
  >
    <div class="max-w-md w-full text-center">
      <div class="text-6xl mb-4">🔒</div>
      <h1 class="text-2xl font-extrabold text-ink mb-2">로그인이 필요해요</h1>
      <p class="text-sm text-ink-muted mb-6">관리자 페이지는 발주인(OWNER) 권한이 있어야 들어올 수 있어요. 먼저 네이버 계정으로 로그인해주세요.</p>
      <div class="flex items-center justify-center gap-2">
        <RouterLink
          to="/login"
          class="rounded-full bg-violet-deep px-6 py-2.5 text-sm font-semibold text-ink hover:bg-violet-deep/80 transition-colors"
        >
          네이버 로그인
        </RouterLink>
        <RouterLink
          to="/"
          class="rounded-full border border-border px-6 py-2.5 text-sm text-ink-muted hover:text-ink hover:border-ink/40 transition-colors"
        >
          홈으로
        </RouterLink>
      </div>
    </div>
  </div>

  <div
    v-else-if="access === 'forbidden'"
    class="min-h-screen grid place-items-center bg-paper text-ink p-8"
  >
    <div class="max-w-md w-full text-center">
      <div class="text-6xl mb-4">🚫</div>
      <h1 class="text-2xl font-extrabold text-ink mb-2">접근 권한이 없어요</h1>
      <p class="text-sm text-ink-muted mb-6">관리자 페이지는 발주인(OWNER) 등급만 들어올 수 있어요. 권한이 필요하면 운영자에게 문의해주세요.</p>
      <RouterLink
        to="/"
        class="inline-block rounded-full bg-violet-deep px-6 py-2.5 text-sm font-semibold text-ink hover:bg-violet-deep/80 transition-colors"
      >
        홈으로 돌아가기
      </RouterLink>
    </div>
  </div>

  <div v-else class="min-h-screen flex bg-paper text-ink">
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
