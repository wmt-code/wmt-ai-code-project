<template>
  <a-layout class="basic-layout">
    <GlobalHeader />
    <a-layout-content class="basic-layout-content">
      <router-view />
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.basic-layout-content {
  flex: 1;
  padding: 24px;
  background-color: #f5f5f5;
}

@media (max-width: 768px) {
  .basic-layout-content {
    padding: 16px;
  }
}
</style>