import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'ADMIN',
  },
  actions: {
    async login(username, password) {
      const resp = await axios.post('/api/auth/login', { username, password })
      this.token = resp.data.token
      this.username = resp.data.username
      this.role = resp.data.role
      localStorage.setItem('token', resp.data.token)
      localStorage.setItem('username', resp.data.username)
      localStorage.setItem('role', resp.data.role)
      axios.defaults.headers.common['Authorization'] = 'Bearer ' + resp.data.token
    },
    logout() {
      this.token = ''
      this.username = ''
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      delete axios.defaults.headers.common['Authorization']
    },
    initAuth() {
      if (this.token) {
        axios.defaults.headers.common['Authorization'] = 'Bearer ' + this.token
      }
    },
  },
})
