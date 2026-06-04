<template>
  <router-view />
</template>

<script>
import { useAuthStore } from './store.js'

export default {
  name: 'App',
  mounted() {
    const store = useAuthStore()
    store.initAuth()
  },
}
</script>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif; background: #f0f2f5; color: #333; }
</style>
