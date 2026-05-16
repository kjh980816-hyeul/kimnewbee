<script setup lang="ts">
import Sidebar from './components/Sidebar.vue';
import HeaderNav from './components/HeaderNav.vue';
</script>

<template>
  <div class="min-h-screen flex bg-paper text-ink">
    <Sidebar />
    <div class="flex-1 min-w-0 flex flex-col">
      <HeaderNav />
      <main class="flex-1 min-w-0">
        <slot />
      </main>
    </div>
  </div>
</template>
