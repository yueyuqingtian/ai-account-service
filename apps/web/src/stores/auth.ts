import { defineStore } from 'pinia'
import { api } from '../api/client'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('web_token') || '',
    username: localStorage.getItem('web_username') || ''
  }),
  actions: {
    async login(username: string, password: string) {
      const res: any = await api.post('/api/auth/login', { username, password })
      this.token = res.data.token
      this.username = res.data.userInfo.username
      localStorage.setItem('web_token', this.token)
      localStorage.setItem('web_username', this.username)
    },
    async sendRegisterCode(email: string) {
      return await api.post('/api/auth/send-register-code', { email })
    },
    async register(username: string, password: string, email: string, verifyCode: string) {
      await api.post('/api/auth/register', { username, password, email, verifyCode })
      await this.login(username, password)
    },
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('web_token')
      localStorage.removeItem('web_username')
    }
  }
})
