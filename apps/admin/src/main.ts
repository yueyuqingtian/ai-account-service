import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import LoginPage from './pages/LoginPage.vue'
import DashboardPage from './pages/DashboardPage.vue'
import ProductsPage from './pages/ProductsPage.vue'
import InventoryPage from './pages/InventoryPage.vue'
import RecordsPage from './pages/RecordsPage.vue'
import PaymentConfigPage from './pages/PaymentConfigPage.vue'
import EmailConfigPage from './pages/EmailConfigPage.vue'
import './style.css'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginPage },
    { path: '/', component: DashboardPage },
    { path: '/products', component: ProductsPage },
    { path: '/inventory', component: InventoryPage },
    { path: '/payment-config', component: PaymentConfigPage },
    { path: '/email-config', component: EmailConfigPage },
    { path: '/records', component: RecordsPage }
  ]
})

createApp(App).use(createPinia()).use(router).mount('#app')
