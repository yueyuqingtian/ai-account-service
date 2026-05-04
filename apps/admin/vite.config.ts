import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/admin': 'http://localhost:8080',
      '/uploads': 'http://localhost:8080'
    }
  }
})
