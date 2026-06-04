<template>
  <div class="login-container">
    <div class="login-card">
      <h2>预算执行报告系统</h2>
      <p style="margin-bottom: 20px; color: #888;">请登录</p>
      <div class="form-group">
        <label>用户名</label>
        <input v-model="username" placeholder="请输入用户名" @keyup.enter="doLogin" />
      </div>
      <div class="form-group">
        <label>密码</label>
        <input v-model="password" type="password" placeholder="请输入密码" @keyup.enter="doLogin" />
      </div>
      <div v-if="error" class="error-msg">{{ error }}</div>
      <button class="btn-login" @click="doLogin" :disabled="loading">{{ loading ? '登录中...' : '登 录' }}</button>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '../store.js'

export default {
  name: 'Login',
  data() {
    return { username: '', password: '', loading: false, error: '' }
  },
  methods: {
    async doLogin() {
      if (!this.username || !this.password) { this.error = '请输入用户名和密码'; return }
      this.loading = true
      this.error = ''
      try {
        const store = useAuthStore()
        await store.login(this.username, this.password)
        if (store.isAdmin) this.$router.push('/admin')
        else this.$router.push('/')
      } catch (e) {
        this.error = e.response?.data?.error || '登录失败'
      } finally {
        this.loading = false
      }
    },
  },
}
</script>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f0f2f5; }
.login-card { background: #fff; border-radius: 8px; padding: 40px; width: 380px; box-shadow: 0 2px 12px rgba(0,0,0,.1); text-align: center; }
h2 { margin-bottom: 4px; color: #1a1a2e; }
.form-group { text-align: left; margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #555; margin-bottom: 4px; }
.form-group input { width: 100%; padding: 8px 12px; border: 1px solid #d9d9d9; border-radius: 4px; font-size: 14px; outline: none; box-sizing: border-box; }
.form-group input:focus { border-color: #1890ff; }
.btn-login { width: 100%; padding: 10px; background: #1890ff; color: #fff; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; }
.btn-login:hover { background: #40a9ff; }
.btn-login:disabled { background: #91caff; cursor: not-allowed; }
.error-msg { color: #ff4d4f; font-size: 13px; margin-bottom: 12px; }
</style>
