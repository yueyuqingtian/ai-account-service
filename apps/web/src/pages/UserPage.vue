<template>
  <section>
    <div class="account-head">
      <div>
        <span class="eyebrow">个人中心</span>
        <h1>我的权益</h1>
        <p class="muted">集中查看订单、CDKey 和兑换记录。</p>
      </div>
      <button @click="load">刷新数据</button>
    </div>
    <div class="tabs">
      <button :class="{ active: tab === 'orders' }" class="secondary" @click="tab = 'orders'">订单</button>
      <button :class="{ active: tab === 'cdkeys' }" class="secondary" @click="tab = 'cdkeys'">CDKey</button>
      <button :class="{ active: tab === 'redeems' }" class="secondary" @click="tab = 'redeems'">兑换记录</button>
    </div>
    <p v-if="message" class="muted">{{ message }}</p>
    <table class="table">
      <thead>
        <tr>
          <th v-for="col in columns" :key="col">{{ col }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.id">
          <td v-for="(col, index) in keys" :key="col" :data-label="columns[index]">{{ row[col] }}</td>
        </tr>
      </tbody>
    </table>
    <div v-if="!rows.length && !message" class="empty-table">
      <strong>暂无数据</strong>
      <span>购买、兑换或刷新后会在这里动态更新。</span>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const tab = ref<'orders' | 'cdkeys' | 'redeems'>('orders')
const rows = ref<any[]>([])
const message = ref('')

const keys = computed(() => {
  if (tab.value === 'orders') return ['order_no', 'product_name_snapshot', 'pay_amount', 'pay_status', 'delivery_status']
  if (tab.value === 'cdkeys') return ['cdk_code', 'product_name', 'status', 'order_no', 'created_at']
  return ['redeem_no', 'cdk_code', 'result', 'fail_reason', 'created_at']
})
const columns = computed(() => keys.value.map((key) => key.replace(/_/g, ' ')))

async function load() {
  if (!auth.token) {
    router.push('/login')
    return
  }
  message.value = ''
  const url = tab.value === 'orders' ? '/api/user/orders' : tab.value === 'cdkeys' ? '/api/user/cdkeys' : '/api/user/redeems'
  try {
    const res: any = await api.get(url)
    rows.value = res.data.records
  } catch (e: any) {
    message.value = e.message
  }
}

watch(tab, load)
onMounted(load)
</script>

<style scoped>
.account-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  margin-bottom: 18px;
}

.account-head h1 {
  margin: 12px 0 4px;
}

.eyebrow {
  display: inline-flex;
  border-radius: 999px;
  padding: 6px 10px;
  background: #fef3c7;
  color: #92400e;
  font-size: 12px;
  font-weight: 900;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.tabs button.active {
  background: #0f766e;
  color: #fff;
}

.empty-table {
  display: grid;
  gap: 8px;
  place-items: center;
  min-height: 160px;
  margin-top: 12px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: rgba(255, 255, 255, .72);
  color: #64748b;
}

.empty-table strong {
  color: #1f2937;
}

@media (max-width: 720px) {
  .account-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
