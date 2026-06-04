<script setup lang="ts">
import Sidebar from './components/Sidebar.vue';
import HeaderNav from './components/HeaderNav.vue';
</script>

<template>
  <div class="min-h-screen bg-paper text-ink">
    <HeaderNav />
    <div class="layout">
      <Sidebar />
      <main class="content min-w-0 flex-1">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  max-width: 1480px;
  margin: 0 auto;
}
.content {
  min-width: 0;
}
</style>
