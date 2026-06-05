<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import {
  fetchMe,
  fetchMyStats,
  updateMyProfileImage,
  updateMyNickname,
  fetchMyPosts,
  fetchMyCommentedPosts,
  fetchMyLikedPosts,
} from '@/api/me';
import { uploadImage } from '@/api/upload';
import { logout } from '@/api/auth';
import { isHttpStatus, errorMessage } from '@/api/error';
import type { CurrentUser, UserStats, MyActivityItem } from '@/types/user';
import type { Tier } from '@/types/offline';

const router = useRouter();

const user = ref<CurrentUser | null>(null);
const stats = ref<UserStats | null>(null);
const loading = ref(true);
const notLoggedIn = ref(false);
const error = ref<string | null>(null);

type ActivityTab = 'posts' | 'comments' | 'liked' | 'scraps';
const activeTab = ref<ActivityTab>('posts');

const activityFetchers: Record<'posts' | 'comments' | 'liked', () => Promise<MyActivityItem[]>> = {
  posts: fetchMyPosts,
  comments: fetchMyCommentedPosts,
  liked: fetchMyLikedPosts,
};
const tabCache = ref<Partial<Record<ActivityTab, MyActivityItem[]>>>({});
const tabLoading = ref(false);
const tabError = ref<string | null>(null);

const currentItems = computed<MyActivityItem[]>(() => tabCache.value[activeTab.value] ?? []);

const boardTypeLabel: Record<string, string> = {
  FREE: '자유',
  FANART: '팬아트',
  CLIP: '클립',
  PET: '반려동물',
  OFFLINE: '오프라인',
  CUSTOM: '게시판',
};

function formatDate(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}

async function loadTab(tab: ActivityTab): Promise<void> {
  if (tab === 'scraps') return; // 스크랩 기능 미구현
  if (tabCache.value[tab]) return; // 이미 로드됨
  tabLoading.value = true;
  tabError.value = null;
  try {
    tabCache.value = { ...tabCache.value, [tab]: await activityFetchers[tab]() };
  } catch (e) {
    tabError.value = errorMessage(e, '목록을 불러올 수 없어요');
  } finally {
    tabLoading.value = false;
  }
}

function selectTab(tab: ActivityTab): void {
  activeTab.value = tab;
  tabError.value = null;
  void loadTab(tab);
}

const tierMeta: Record<Tier, { label: string; emoji: string }> = {
  seed: { label: '새싹', emoji: '🌱' },
  pepper: { label: '고추', emoji: '🌶' },
  corn: { label: '옥수수', emoji: '🌽' },
  owner: { label: '밭주인', emoji: '👑' },
};

const TIER_THRESHOLDS: Record<Tier, { floor: number; next: number | null }> = {
  seed: { floor: 0, next: 100 },
  pepper: { floor: 100, next: 500 },
  corn: { floor: 500, next: null },
  owner: { floor: 0, next: null },
};

const tierOrder: Tier[] = ['seed', 'pepper', 'corn', 'owner'];

const progress = computed(() => {
  if (!user.value) return { pct: 0, current: 0, needed: 0, nextTier: null as Tier | null };
  const t = TIER_THRESHOLDS[user.value.tier];
  if (t.next === null) {
    return { pct: 100, current: user.value.points - t.floor, needed: 0, nextTier: null };
  }
  const span = t.next - t.floor;
  const current = Math.max(0, user.value.points - t.floor);
  const pct = Math.min(100, Math.round((current / span) * 100));
  const idx = tierOrder.indexOf(user.value.tier);
  return { pct, current, needed: span, nextTier: tierOrder[idx + 1] ?? null };
});

const joinedLabel = computed(() => {
  if (!user.value) return '';
  const d = new Date(user.value.createdAt);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
});

onMounted(async () => {
  try {
    const [me, myStats] = await Promise.all([fetchMe(), fetchMyStats()]);
    user.value = me;
    stats.value = myStats;
    void loadTab('posts');
  } catch (e) {
    if (isHttpStatus(e, 401)) {
      notLoggedIn.value = true;
    } else {
      error.value = e instanceof Error ? e.message : '내 정보를 불러올 수 없어요';
    }
  } finally {
    loading.value = false;
  }
});

async function onLogout(): Promise<void> {
  await logout();
  await router.push({ name: 'home' });
}

const avatarBusy = ref(false);
const avatarError = ref<string | null>(null);

async function onAvatarPick(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file || !user.value) return;
  avatarBusy.value = true;
  avatarError.value = null;
  try {
    const uploaded = await uploadImage(file);
    const updated = await updateMyProfileImage(uploaded.url);
    user.value = updated;
  } catch (e) {
    if (isHttpStatus(e, 400)) {
      avatarError.value = '이미지 형식/크기를 확인해주세요 (10MB, png/jpg/webp/gif)';
    } else if (isHttpStatus(e, 401)) {
      avatarError.value = '로그인이 필요해요';
    } else {
      avatarError.value = e instanceof Error ? e.message : '프로필 사진 변경 실패';
    }
  } finally {
    avatarBusy.value = false;
    input.value = '';
  }
}

async function onAvatarRemove(): Promise<void> {
  if (!user.value || !user.value.profileImage) return;
  if (!confirm('프로필 사진을 기본으로 되돌릴까요?')) return;
  avatarBusy.value = true;
  avatarError.value = null;
  try {
    const updated = await updateMyProfileImage(null);
    user.value = updated;
  } catch (e) {
    avatarError.value = e instanceof Error ? e.message : '되돌리기 실패';
  } finally {
    avatarBusy.value = false;
  }
}

const NICKNAME_INTERVAL_DAYS = 30;
const nicknameEditing = ref(false);
const nicknameDraft = ref('');
const nicknameBusy = ref(false);
const nicknameError = ref<string | null>(null);

const nicknameCooldown = computed<{ remainingDays: number; canChange: boolean }>(() => {
  if (!user.value || !user.value.nicknameChangedAt) {
    return { remainingDays: 0, canChange: true };
  }
  const last = new Date(user.value.nicknameChangedAt).getTime();
  const elapsedMs = Date.now() - last;
  const totalMs = NICKNAME_INTERVAL_DAYS * 24 * 60 * 60 * 1000;
  if (elapsedMs >= totalMs) return { remainingDays: 0, canChange: true };
  const remainingDays = Math.ceil((totalMs - elapsedMs) / (24 * 60 * 60 * 1000));
  return { remainingDays, canChange: false };
});

function startEditNickname(): void {
  if (!user.value) return;
  if (!nicknameCooldown.value.canChange) return;
  nicknameDraft.value = user.value.nickname;
  nicknameError.value = null;
  nicknameEditing.value = true;
}

function cancelEditNickname(): void {
  nicknameEditing.value = false;
  nicknameError.value = null;
}

async function saveNickname(): Promise<void> {
  if (!user.value) return;
  const next = nicknameDraft.value.trim();
  if (!next) {
    nicknameError.value = '닉네임을 입력해주세요';
    return;
  }
  if (next === user.value.nickname) {
    nicknameEditing.value = false;
    return;
  }
  nicknameBusy.value = true;
  nicknameError.value = null;
  try {
    user.value = await updateMyNickname(next);
    nicknameEditing.value = false;
  } catch (e) {
    if (isHttpStatus(e, 409)) {
      nicknameError.value = '이미 사용 중인 닉네임이에요';
    } else if (isHttpStatus(e, 400)) {
      const code = (e as { response?: { data?: { code?: string } } }).response?.data?.code;
      if (code === 'NICKNAME_TOO_SOON') {
        nicknameError.value = `30일에 한 번만 변경할 수 있어요 (${nicknameCooldown.value.remainingDays}일 후 가능)`;
      } else {
        nicknameError.value = '닉네임 형식이 올바르지 않아요 (2~20자)';
      }
    } else if (isHttpStatus(e, 401)) {
      nicknameError.value = '로그인이 필요해요';
    } else {
      nicknameError.value = e instanceof Error ? e.message : '닉네임 변경에 실패했어요';
    }
  } finally {
    nicknameBusy.value = false;
  }
}
</script>

<template>
  <div class="p-8">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">내 프로필</span>
    </nav>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <div
      v-else-if="notLoggedIn"
      class="glass p-12 text-center"
    >
      <p class="text-ink mb-4">로그인이 필요해요</p>
      <RouterLink :to="{ name: 'login' }" class="btn-primary inline-block">로그인하러 가기</RouterLink>
    </div>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="user">
      <section
        class="relative rounded-2xl p-6 overflow-hidden"
        style="background: linear-gradient(150deg, rgba(95, 199, 107, 0.18), rgba(28, 28, 31, 0.7)); border: 1px solid rgba(95, 199, 107, 0.2)"
      >
        <div class="flex items-center gap-5">
          <label
            class="relative w-24 h-24 rounded-full bg-violet/40 border-4 border-violet/30 grid place-items-center text-4xl shrink-0 overflow-hidden cursor-pointer group"
            :class="{ 'pointer-events-none opacity-60': avatarBusy }"
            title="클릭해서 프로필 사진 변경"
          >
            <input type="file" accept="image/*" class="hidden" @change="onAvatarPick" />
            <img v-if="user.profileImage" :src="user.profileImage" :alt="user.nickname" class="w-full h-full object-cover" />
            <span v-else>{{ user.nickname[0] }}</span>
            <div class="absolute inset-0 bg-paper/0 group-hover:bg-paper/70 transition-colors flex items-center justify-center text-[10px] text-ink font-semibold opacity-0 group-hover:opacity-100">
              📷<br />변경
            </div>
            <div v-if="avatarBusy" class="absolute inset-0 bg-paper/80 flex items-center justify-center text-[10px] text-violet font-semibold">
              올리는 중...
            </div>
          </label>
          <div class="min-w-0 flex-1">
            <div v-if="!nicknameEditing" class="flex items-center gap-2 flex-wrap">
              <h2 class="text-2xl font-extrabold text-ink" style="font-family: var(--font-serif)">{{ user.nickname }}</h2>
              <button
                v-if="nicknameCooldown.canChange"
                type="button"
                class="text-xs rounded-md border border-ink/20 px-2 py-0.5 text-ink/80 hover:bg-paper/20 transition-colors"
                @click="startEditNickname"
              >
                ✎ 닉네임 변경
              </button>
              <span
                v-else
                class="text-[10px] text-ink/70"
                :title="`닉네임 변경은 30일에 한 번만 가능해요`"
              >
                🔒 {{ nicknameCooldown.remainingDays }}일 후 변경 가능
              </span>
              <span class="px-2 py-0.5 rounded-full text-xs bg-paper/30 text-ink font-semibold">
                {{ tierMeta[user.tier].emoji }} {{ tierMeta[user.tier].label }}
              </span>
            </div>
            <div v-else>
              <div class="flex items-center gap-2 flex-wrap">
                <input
                  v-model="nicknameDraft"
                  type="text"
                  maxlength="20"
                  placeholder="새 닉네임 (2~20자)"
                  class="field-input w-56 text-lg font-bold"
                  :disabled="nicknameBusy"
                  @keydown.enter.prevent="saveNickname"
                  @keydown.escape.prevent="cancelEditNickname"
                />
                <button
                  type="button"
                  class="btn-primary disabled:opacity-50"
                  :disabled="nicknameBusy"
                  @click="saveNickname"
                >
                  {{ nicknameBusy ? '저장중' : '저장' }}
                </button>
                <button
                  type="button"
                  class="rounded-md bg-surface border border-border px-4 py-2 text-sm text-ink hover:bg-elevated disabled:opacity-50"
                  :disabled="nicknameBusy"
                  @click="cancelEditNickname"
                >
                  취소
                </button>
              </div>
              <p v-if="nicknameError" class="mt-2 text-xs text-cheek">⚠ {{ nicknameError }}</p>
              <p class="mt-1 text-[11px] text-ink-muted">한 번 변경하면 30일간 다시 바꿀 수 없어요</p>
            </div>
            <p class="mt-1 text-xs text-ink-muted">
              고추밭 가입일 · {{ joinedLabel }}
              <span v-if="stats && stats.attendanceStreak > 0" class="ml-2">· {{ stats.attendanceStreak }}일 연속 출석중 ⭐</span>
            </p>
            <div v-if="stats" class="mt-3 flex items-center gap-5 text-sm">
              <div><span class="font-bold text-ink">{{ stats.postCount }}</span> <span class="text-ink-muted text-xs">글</span></div>
              <div><span class="font-bold text-ink">{{ stats.commentCount }}</span> <span class="text-ink-muted text-xs">댓글</span></div>
              <div><span class="font-bold text-ink">{{ stats.likeGivenCount }}</span> <span class="text-ink-muted text-xs">받은 ♥</span></div>
            </div>
            <p v-if="avatarError" class="mt-2 text-xs text-cheek">⚠ {{ avatarError }}</p>
          </div>
          <button
            v-if="user.profileImage"
            type="button"
            :disabled="avatarBusy"
            class="rounded-full border border-ink/30 px-4 py-1.5 text-sm text-ink hover:bg-paper/20 transition-colors shrink-0 disabled:opacity-50"
            @click="onAvatarRemove"
          >
            프사 기본으로
          </button>
        </div>
      </section>

      <section class="mt-4 glass card-pad">
        <div class="flex items-center justify-between mb-2 text-xs">
          <div class="flex items-center gap-2">
            <span class="font-semibold text-ink">
              {{ tierMeta[user.tier].emoji }}{{ tierMeta[user.tier].label }}
            </span>
            <template v-if="progress.nextTier">
              <span class="text-ink-muted">→</span>
              <span class="font-semibold text-ink">
                {{ tierMeta[progress.nextTier].emoji }}{{ tierMeta[progress.nextTier].label }}
              </span>
              <span class="text-ink-muted">활동점수 {{ progress.current }} / {{ progress.needed }} 필요</span>
            </template>
            <span v-else class="text-ink-muted">최고 등급 달성!</span>
          </div>
          <span class="font-bold text-violet">{{ progress.pct }}%</span>
        </div>
        <div class="relative h-2.5 rounded-full bg-elevated overflow-hidden">
          <div
            class="h-full bg-gradient-to-r from-green via-green-bright to-corn transition-all"
            :style="{ width: progress.pct + '%' }"
          ></div>
        </div>
        <div class="mt-2 flex justify-between text-[10px] text-ink-muted">
          <span>🌱 새싹</span>
          <span>🌶 고추</span>
          <span>🌽 옥수수</span>
          <span>👑 밭주인</span>
        </div>
      </section>

      <section class="mt-4">
        <div class="flex items-center gap-1 border-b border-border">
          <button
            type="button"
            class="px-4 py-2.5 text-sm border-b-2 -mb-px transition-colors"
            :class="activeTab === 'posts' ? 'border-pepper text-ink font-semibold' : 'border-transparent text-ink-muted hover:text-ink'"
            @click="selectTab('posts')"
          >
            내가 쓴 글 <span class="text-ink-muted ml-0.5 text-xs">{{ stats?.postCount ?? 0 }}</span>
          </button>
          <button
            type="button"
            class="px-4 py-2.5 text-sm border-b-2 -mb-px transition-colors"
            :class="activeTab === 'comments' ? 'border-pepper text-ink font-semibold' : 'border-transparent text-ink-muted hover:text-ink'"
            @click="selectTab('comments')"
          >
            댓글 단 글 <span class="text-ink-muted ml-0.5 text-xs">{{ stats?.commentCount ?? 0 }}</span>
          </button>
          <button
            type="button"
            class="px-4 py-2.5 text-sm border-b-2 -mb-px transition-colors"
            :class="activeTab === 'liked' ? 'border-pepper text-ink font-semibold' : 'border-transparent text-ink-muted hover:text-ink'"
            @click="selectTab('liked')"
          >
            좋아요한 글 <span class="text-ink-muted ml-0.5 text-xs">{{ stats?.likeGivenCount ?? 0 }}</span>
          </button>
          <button
            type="button"
            class="px-4 py-2.5 text-sm border-b-2 -mb-px transition-colors"
            :class="activeTab === 'scraps' ? 'border-pepper text-ink font-semibold' : 'border-transparent text-ink-muted hover:text-ink'"
            @click="selectTab('scraps')"
          >
            스크랩 <span class="text-ink-muted ml-0.5 text-xs">0</span>
          </button>
        </div>

        <div class="rounded-b-2xl bg-surface border-x border-b border-border">
          <p v-if="activeTab === 'scraps'" class="px-5 py-10 text-center text-sm text-ink-muted">
            스크랩 기능은 곧 추가될 예정이에요 🌶
          </p>
          <p v-else-if="tabLoading" class="px-5 py-10 text-center text-sm text-ink-muted">
            불러오는 중...
          </p>
          <p v-else-if="tabError" class="px-5 py-10 text-center text-sm text-cheek">
            ⚠ {{ tabError }}
          </p>
          <p v-else-if="currentItems.length === 0" class="px-5 py-10 text-center text-sm text-ink-muted">
            <template v-if="activeTab === 'posts'">아직 작성한 글이 없어요</template>
            <template v-else-if="activeTab === 'comments'">아직 댓글을 단 글이 없어요</template>
            <template v-else>아직 좋아요한 글이 없어요</template>
          </p>
          <ul v-else class="divide-y divide-border">
            <li v-for="item in currentItems" :key="item.boardType + '-' + item.id">
              <RouterLink
                :to="item.link"
                class="flex items-center gap-3 px-5 py-3.5 hover:bg-elevated/60 transition-colors"
              >
                <img
                  v-if="item.thumbnailUrl"
                  :src="item.thumbnailUrl"
                  alt=""
                  class="w-12 h-12 rounded-lg object-cover shrink-0 bg-elevated"
                />
                <div class="min-w-0 flex-1">
                  <div class="flex items-center gap-1.5">
                    <span class="text-[10px] px-1.5 py-0.5 rounded bg-paper/40 text-ink-muted shrink-0">
                      {{ boardTypeLabel[item.boardType] ?? item.boardType }}
                    </span>
                    <span class="text-sm text-ink font-medium truncate">{{ item.title }}</span>
                  </div>
                  <p class="mt-0.5 text-xs text-ink-muted truncate">{{ item.preview }}</p>
                </div>
                <div class="flex items-center gap-2.5 text-[11px] text-ink-muted shrink-0">
                  <span title="댓글">💬 {{ item.commentCount }}</span>
                  <span title="좋아요">♥ {{ item.likeCount }}</span>
                  <span class="hidden sm:inline">{{ formatDate(item.createdAt) }}</span>
                </div>
              </RouterLink>
            </li>
          </ul>
        </div>
      </section>

      <div class="mt-6 text-right">
        <button
          type="button"
          class="text-xs text-ink-muted hover:text-cheek transition-colors"
          @click="onLogout"
        >
          로그아웃
        </button>
      </div>
    </template>
  </div>
</template>
