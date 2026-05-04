<template>
  <section>
    <h1>订单与记录</h1>
    <div class="row">
      <button class="secondary" :class="{ active: tab === 'orders' }" @click="tab = 'orders'">订单</button>
      <button class="secondary" :class="{ active: tab === 'payments' }" @click="tab = 'payments'">支付</button>
      <button class="secondary" :class="{ active: tab === 'cdkeys' }" @click="tab = 'cdkeys'">CDKey</button>
      <button class="secondary" :class="{ active: tab === 'redeems' }" @click="tab = 'redeems'">兑换</button>
      <button class="secondary" :class="{ active: tab === 'logs' }" @click="tab = 'logs'">日志</button>
      <button class="secondary" :class="{ active: tab === 'verification-logs' }" @click="tab = 'verification-logs'">验证码日志</button>
      <input v-model="keyword" placeholder="搜索关键字" @keyup.enter="load" />
      <button @click="load">查询</button>
    </div>
    <p v-if="message" class="muted">{{ message }}</p>
    <table class="table records-table">
      <thead>
        <tr>
          <th v-for="col in columns" :key="col">{{ col }}</th>
          <th v-if="tab === 'orders'">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.id">
          <td v-for="col in keys" :key="col">{{ row[col] }}</td>
          <td v-if="tab === 'orders'">
            <button v-if="row.pay_status === 'UNPAID'" class="secondary" @click="markPaid(row.order_no)">确认收款</button>
            <span v-else class="status-pill">已处理</span>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { api } from '../api/client'

const tab = ref<'orders' | 'payments' | 'cdkeys' | 'redeems' | 'logs' | 'verification-logs'>('orders')
const rows = ref<any[]>([])
const message = ref('')
const keyword = ref('')

const keys = computed(() => {
  if (tab.value === 'orders') return ['order_no', 'user_id', 'pay_amount', 'pay_status', 'delivery_status', 'created_at']
  if (tab.value === 'payments') return ['payment_no', 'order_no', 'channel', 'amount', 'status', 'paid_at']
  if (tab.value === 'cdkeys') return ['cdk_code', 'owner_user_id', 'order_no', 'status', 'created_at']
  if (tab.value === 'redeems') return ['redeem_no', 'user_id', 'cdk_code', 'result', 'fail_reason']
  if (tab.value === 'verification-logs') return ['user_id', 'resource_account', 'result', 'code_value', 'fail_reason', 'created_at']
  return ['admin_id', 'module', 'operation_type', 'target_id', 'created_at']
})

const labelMap: Record<string, string> = {
  order_no: '订单号',
  user_id: '用户',
  pay_amount: '金额',
  pay_status: '支付状态',
  delivery_status: '交付状态',
  created_at: '创建时间',
  payment_no: '支付单号',
  channel: '渠道',
  amount: '金额',
  status: '状态',
  paid_at: '支付时间',
  cdk_code: 'CDKey',
  owner_user_id: '归属用户',
  redeem_no: '兑换号',
  result: '结果',
  fail_reason: '失败原因',
  resource_account: '账号',
  code_value: '验证码',
  admin_id: '管理员',
  module: '模块',
  operation_type: '操作',
  target_id: '对象'
}
const columns = computed(() => keys.value.map((key) => labelMap[key] || key))

async function load() {
  try {
    const res: any = await api.get(`/admin/${tab.value}`, { params: { keyword: keyword.value } })
    rows.value = res.data.records
  } catch (e: any) {
    message.value = e.message
  }
}

async function markPaid(orderNo: string) {
  try {
    await api.post(`/admin/orders/${orderNo}/mark-paid`)
    message.value = '收款已确认'
    await load()
  } catch (e: any) {
    message.value = e.message
  }
}

watch(tab, load)
onMounted(load)
</script>

<style scoped>
.records-table {
  margin-top: 16px;
}

.row button.active {
  background: #1f6f8b;
  color: #fff;
}
</style>
