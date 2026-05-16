<script setup lang="ts">
import Sidebar from './components/Sidebar.vue';
</script>

<template>
  <div class="min-h-screen flex bg-paper text-ink">
    <Sidebar />
    <main class="flex-1 min-w-0">
      <slot />
    </main>
  </div>
</template>
