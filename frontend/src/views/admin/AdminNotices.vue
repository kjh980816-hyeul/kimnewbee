<script setup lang="ts">
import AdminPlaceholder from './AdminPlaceholder.vue';
</script>

<template>
  <AdminPlaceholder
    title="공지사항 관리"
    emoji="📢"
    description="공지사항 게시·수정·고정·만료 관리를 한 곳에서 처리해요"
    :features="[
      '공지 작성/수정/삭제 (기존 NoticeController 그대로 사용)',
      '상단 고정/만료일 설정 (백엔드 필드 추가 필요)',
      '카테고리 분류 (이벤트/공지/긴급)',
      '예약 발행 (특정 시각에 자동 게시)',
    ]"
  />
</template>
