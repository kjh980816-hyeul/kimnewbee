import { ref } from 'vue';

// 모바일 사이드바 드로어 열림 상태(헤더 햄버거 ↔ 사이드바 공유).
const open = ref(false);

export function useSidebarDrawer() {
  return {
    open,
    toggle: () => {
      open.value = !open.value;
    },
    close: () => {
      open.value = false;
    },
  };
}
