<template>
  <section>
    <div class="page-head">
      <div>
        <h1>运营仪表盘</h1>
        <p class="muted">收入、订单、交付与库存风险的实时概览。</p>
      </div>
      <button @click="load">刷新</button>
    </div>

    <p v-if="message" class="muted">{{ message }}</p>

    <div class="grid stat-grid">
      <div v-for="item in statCards" :key="item.key" class="card stat-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.hint }}</p>
      </div>
    </div>

    <div class="business-grid">
      <section class="panel wide-panel">
        <div class="panel-head">
          <h3>近 7 日收入</h3>
          <span class="status-pill">按支付时间</span>
        </div>
        <div class="trend">
          <div v-for="item in revenueTrend" :key="String(item.day)" class="trend-row">
            <span>{{ formatDay(item.day) }}</span>
            <div class="bar-track">
              <i :style="{ width: barWidth(item.revenue) }"></i>
            </div>
            <strong>¥{{ money(item.revenue) }}</strong>
          </div>
          <p v-if="!revenueTrend.length" class="muted">暂无支付收入。</p>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h3>支付渠道</h3>
          <span class="status-pill">成功支付</span>
        </div>
        <div class="mini-list">
          <div v-for="item in channelRevenue" :key="item.channel">
            <span>{{ item.channel }}</span>
            <strong>{{ item.payments }} 笔 / ¥{{ money(item.revenue) }}</strong>
          </div>
          <p v-if="!channelRevenue.length" class="muted">暂无支付记录。</p>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h3>订单状态</h3>
          <span class="status-pill">全量订单</span>
        </div>
        <div class="mini-list">
          <div v-for="item in orderStatus" :key="`${item.order_status}-${item.pay_status}-${item.delivery_status}`">
            <span>{{ item.order_status }} / {{ item.pay_status }} / {{ item.delivery_status }}</span>
            <strong>{{ item.total }}</strong>
          </div>
          <p v-if="!orderStatus.length" class="muted">暂无订单。</p>
        </div>
      </section>
    </div>

    <div class="table-grid">
      <section class="panel">
        <div class="panel-head">
          <h3>库存预警</h3>
          <span class="status-pill">可用库存优先</span>
        </div>
        <table class="table compact-table">
          <thead>
            <tr>
              <th>商品</th>
              <th>状态</th>
              <th>可用</th>
              <th>预占</th>
              <th>已交付</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in lowStockProducts" :key="item.id">
              <td>{{ item.name }}</td>
              <td>{{ item.status }}</td>
              <td>{{ item.available_stock }}</td>
              <td>{{ item.reserved_stock }}</td>
              <td>{{ item.assigned_stock }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h3>最近订单</h3>
          <span class="status-pill">已脱敏</span>
        </div>
        <table class="table compact-table">
          <thead>
            <tr>
              <th>订单</th>
              <th>商品</th>
              <th>金额</th>
              <th>支付</th>
              <th>交付</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in recentOrders" :key="item.order_no">
              <td>{{ item.order_no }}</td>
              <td>{{ item.product_name_snapshot }}</td>
              <td>¥{{ money(item.pay_amount) }}</td>
              <td>{{ item.pay_status }}</td>
              <td>{{ item.delivery_status }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import { useAdminAuthStore } from '../stores/auth'

const auth = useAdminAuthStore()
const router = useRouter()
const stats = ref<Record<string, any>>({})
const message = ref('')

const revenueTrend = computed<any[]>(() => stats.value.revenueTrend || [])
const channelRevenue = computed<any[]>(() => stats.value.channelRevenue || [])
const orderStatus = computed<any[]>(() => stats.value.orderStatus || [])
const lowStockProducts = computed<any[]>(() => stats.value.lowStockProducts || [])
const recentOrders = computed<any[]>(() => stats.value.recentOrders || [])
const maxRevenue = computed(() => Math.max(1, ...revenueTrend.value.map((item) => Number(item.revenue || 0))))

const statCards = computed(() => [
  { key: 'totalRevenue', label: '累计收入', value: `¥${money(stats.value.totalRevenue)}`, hint: '已支付订单收入' },
  { key: 'todayRevenue', label: '今日收入', value: `¥${money(stats.value.todayRevenue)}`, hint: '今日已支付金额' },
  { key: 'orders', label: '订单数', value: stats.value.orders || 0, hint: `支付转化率 ${stats.value.payConversionRate || 0}%` },
  { key: 'redeems', label: '成功兑换', value: stats.value.redeems || 0, hint: `兑换率 ${stats.value.redeemRate || 0}%` },
  { key: 'availableInventory', label: '可用库存', value: stats.value.availableInventory || 0, hint: `预占 ${stats.value.reservedInventory || 0} / 已交付 ${stats.value.assignedInventory || 0}` },
  { key: 'products', label: '商品数', value: stats.value.products || 0, hint: '当前商品池规模' }
])

async function load() {
  if (!auth.token) {
    router.push('/login')
    return
  }
  message.value = ''
  try {
    const res: any = await api.get('/admin/dashboard')
    stats.value = res.data
  } catch (e: any) {
    message.value = e.message
  }
}

function money(value: any) {
  return Number(value || 0).toFixed(2)
}

function barWidth(value: any) {
  return `${Math.max(4, Math.round((Number(value || 0) / maxRevenue.value) * 100))}%`
}

function formatDay(value: any) {
  return String(value || '').slice(5, 10) || '-'
}

onMounted(load)
</script>

<style scoped>
.stat-card {
  display: grid;
  gap: 8px;
}

.stat-card span {
  color: #667982;
  font-size: 13px;
  font-weight: 900;
}

.stat-card strong {
  font-size: 30px;
  color: #1f6f8b;
}

.stat-card p {
  margin: 0;
  color: #667982;
}

.business-grid, .table-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, .82fr);
  gap: 14px;
  margin-top: 16px;
}

.wide-panel {
  grid-row: span 2;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-head h3 {
  margin: 0;
}

.trend {
  display: grid;
  gap: 12px;
}

.trend-row {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr) 92px;
  align-items: center;
  gap: 12px;
}

.bar-track {
  height: 10px;
  border-radius: 999px;
  background: #e6eef2;
  overflow: hidden;
}

.bar-track i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #1f6f8b, #35a6a0);
}

.mini-list {
  display: grid;
  gap: 10px;
}

.mini-list div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid #e7eef2;
  padding-bottom: 10px;
}

.mini-list div:last-child {
  border-bottom: 0;
}

.mini-list span {
  color: #667982;
}

.compact-table th, .compact-table td {
  padding: 10px;
  font-size: 13px;
}

@media (max-width: 980px) {
  .business-grid, .table-grid {
    grid-template-columns: 1fr;
  }

  .wide-panel {
    grid-row: auto;
  }
}
</style>
