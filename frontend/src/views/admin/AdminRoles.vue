<script setup lang="ts">
import AdminPlaceholder from './AdminPlaceholder.vue';
</script>

<template>
  <AdminPlaceholder
    title="등급·권한"
    emoji="🏅"
    description="회원 등급별 권한 정책과 자동 승급 규칙을 관리할 수 있어요"
    :features="[
      '등급 정의 (새싹/고추/옥수수/발주인) 임계값 설정',
      '활동점수 자동 승급 규칙 (글/댓글/좋아요별 가중치)',
      '게시판별 읽기·쓰기·댓글 권한 매트릭스',
      '강등/제재 정책 (정지·차단·블랙리스트)',
    ]"
  />
</template>
