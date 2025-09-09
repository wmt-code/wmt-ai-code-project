<template>
  <a-layout class="min-h-screen flex flex-col bg-slate-50">
    <GlobalHeader />
    <a-layout-content class="flex-1 py-6 md:py-4">
      <router-view />
    </a-layout-content>
    <GlobalFooter />
  </a-layout>

</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped></style>
