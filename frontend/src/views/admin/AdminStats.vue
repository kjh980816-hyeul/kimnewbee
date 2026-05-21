<script setup lang="ts">
import AdminPlaceholder from './AdminPlaceholder.vue';
</script>

<template>
  <AdminPlaceholder
    title="통계"
    emoji="📈"
    description="일자별 방문/가입/글·댓글·좋아요 트렌드와 게시판별 활성도를 한눈에"
    :features="[
      '일/주/월별 방문자 추이 그래프 (visit tracking 백엔드 추가 필요)',
      '신규 가입자/이탈자 추이',
      '게시판별 활성도 비교 (글 수, 댓글 수, 좋아요 수)',
      '시간대별 트래픽 히트맵',
      '치지직 방송과의 상관관계 (방송 시간대 글 증가율)',
    ]"
  />
</template>
