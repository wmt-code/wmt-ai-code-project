<template>
  <a-layout-footer class="global-footer">
    <div class="footer-content">
      <div class="footer-info">
        <p class="copyright">© {{ currentYear }} WMT AI 零代码平台</p>
      </div>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.global-footer {
  background: #001529;
  color: rgba(255, 255, 255, 0.65);
  padding: 24px 0;
  text-align: center;
  position: relative;
  bottom: 0;
  width: 100%;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.copyright {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.footer-links {
  display: flex;
  gap: 24px;
  justify-content: center;
  flex-wrap: wrap;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #fff;
}

@media (max-width: 768px) {
  .global-footer {
    padding: 16px 0;
  }

  .footer-content {
    padding: 0 16px;
  }

  .footer-links {
    gap: 16px;
  }

  .footer-links a {
    font-size: 13px;
  }
}
</style>
