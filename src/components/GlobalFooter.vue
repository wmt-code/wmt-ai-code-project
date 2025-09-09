<template>
  <a-layout-footer class="relative w-full bg-[#001529] text-white/70 py-6 text-center">
    <div class="container-1200">
      <div class="flex flex-col items-center gap-2">
        <p class="m-0 text-sm leading-relaxed">© {{ currentYear }} WMT AI 零代码平台</p>
      </div>
    </div>
  </a-layout-footer>

</template>

<script setup lang="ts">
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped></style>
