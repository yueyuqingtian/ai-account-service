import { defineStore } from 'pinia';
import { api } from '../api/client';
export const useAdminAuthStore = defineStore('adminAuth', {
    state: () => ({
        token: localStorage.getItem('admin_token') || '',
        username: localStorage.getItem('admin_username') || ''
    }),
    actions: {
        async login(username, password) {
            const res = await api.post('/admin/auth/login', { username, password });
            this.token = res.data.token;
            this.username = res.data.adminInfo.username;
            localStorage.setItem('admin_token', this.token);
            localStorage.setItem('admin_username', this.username);
        },
        logout() {
            this.token = '';
            this.username = '';
            localStorage.removeItem('admin_token');
            localStorage.removeItem('admin_username');
        }
    }
});
//# sourceMappingURL=auth.js.map