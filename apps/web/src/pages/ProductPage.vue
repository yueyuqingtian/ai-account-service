<template>
  <section v-if="product" class="product-page">
    <div class="detail-layout">
      <div class="media-panel">
        <img :src="product.cover_url || fallback" alt="" class="hero" />
      </div>

      <div class="buy-panel">
        <span class="status">现货 {{ product.available_stock }}</span>
        <h1>{{ product.name }}</h1>
        <p class="muted">{{ product.subtitle }}</p>
        <div class="price-row">
          <span class="price">¥{{ product.price }}</span>
          <span v-if="product.original_price" class="original">¥{{ product.original_price }}</span>
        </div>
        <p class="description">{{ product.description }}</p>

        <div class="checkout-box">
          <label>支付方式</label>
          <select v-model="channel">
            <option value="ALIPAY">支付宝</option>
            <option value="WECHAT">微信支付</option>
            <option value="MOCK">测试支付</option>
          </select>
          <button class="buy-button" :disabled="submitting" @click="buy">{{ submitting ? '处理中...' : '确认购买' }}</button>
          <RouterLink class="muted center-link" to="/redeem">已有 CDKey，去兑换</RouterLink>
        </div>
      </div>
    </div>

    <div v-if="payment" class="payment-panel">
      <div>
        <span class="eyebrow">待支付</span>
        <h2>订单已创建</h2>
        <p class="muted">完成付款后，等待系统或管理员确认收款。</p>
      </div>
      <div class="qr-box" v-if="qrSrc">
        <img :src="qrSrc" alt="收款码" />
      </div>
      <div class="payment-meta">
        <p><strong>订单号</strong><span>{{ payment.orderNo }}</span></p>
        <p><strong>支付单</strong><span>{{ payment.paymentNo }}</span></p>
        <p><strong>渠道</strong><span>{{ payment.channel }}</span></p>
      </div>
      <button v-if="payment.channel === 'MOCK'" @click="mockSuccess">测试支付成功</button>
    </div>

    <p v-if="message" class="muted message">{{ message }}</p>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api/client'
import { useAuthStore } from '../stores/auth'

const fallback = 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=900'
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const product = ref<any>(null)
const channel = ref('ALIPAY')
const submitting = ref(false)
const message = ref('')
const payment = ref<any>(null)

const qrSrc = computed(() => {
  const url = payment.value?.payUrl || ''
  if (!url || url.startsWith('weixin://') || url.includes('/pay/result')) return ''
  return url
})

async function load() {
  try {
    const res: any = await api.get(`/api/products/${route.params.id}`)
    product.value = res.data
  } catch (e: any) {
    message.value = e.message
  }
}

async function buy() {
  if (!auth.token) {
    router.push('/login')
    return
  }
  submitting.value = true
  message.value = ''
  try {
    const order: any = await api.post('/api/orders', { productId: Number(route.params.id), quantity: 1, clientType: 'WEB' })
    const payRes: any = await api.post('/api/payments/create', { orderNo: order.data.orderNo, channel: channel.value })
    payment.value = { ...payRes.data, orderNo: order.data.orderNo }
    message.value = '订单已创建'
  } catch (e: any) {
    message.value = e.message
  } finally {
    submitting.value = false
  }
}

async function mockSuccess() {
  if (!payment.value) return
  submitting.value = true
  try {
    await api.post('/api/payments/mock-success', { paymentNo: payment.value.paymentNo })
    message.value = '支付成功，CDKey 已发放'
    router.push('/user')
  } catch (e: any) {
    message.value = e.message
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 430px;
  gap: 24px;
  align-items: start;
}

.media-panel, .buy-panel, .payment-panel {
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 18px 48px rgba(15, 23, 42, .08);
}

.media-panel {
  padding: 14px;
}

.hero {
  width: 100%;
  aspect-ratio: 16 / 11;
  object-fit: cover;
  border-radius: 8px;
}

.buy-panel {
  padding: 26px;
}

.buy-panel h1 {
  margin: 16px 0 8px;
  font-size: 34px;
  line-height: 1.18;
  letter-spacing: 0;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin: 18px 0;
}

.original {
  color: #94a3b8;
  text-decoration: line-through;
}

.description {
  color: #475569;
  line-height: 1.75;
}

.checkout-box {
  display: grid;
  gap: 12px;
  margin-top: 22px;
  padding: 16px;
  border-radius: 8px;
  background: #f1f5f9;
}

.checkout-box label {
  font-weight: 900;
}

.buy-button {
  width: 100%;
  padding: 14px;
}

.center-link {
  text-align: center;
}

.payment-panel {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 180px minmax(240px, 1fr) auto;
  gap: 18px;
  align-items: center;
  margin-top: 22px;
  padding: 22px;
}

.eyebrow {
  display: inline-flex;
  border-radius: 999px;
  padding: 5px 9px;
  background: #fef3c7;
  color: #92400e;
  font-size: 12px;
  font-weight: 900;
}

.payment-panel h2 {
  margin: 10px 0 6px;
}

.qr-box {
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #f8fafc;
  padding: 10px;
}

.qr-box img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: contain;
}

.payment-meta {
  display: grid;
  gap: 8px;
}

.payment-meta p {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: 10px;
  margin: 0;
}

.payment-meta span {
  color: #475569;
  overflow-wrap: anywhere;
}

.message {
  margin-top: 18px;
}

@media (max-width: 920px) {
  .detail-layout, .payment-panel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .buy-panel {
    padding: 20px;
  }

  .buy-panel h1 {
    font-size: 28px;
  }

  .payment-meta p {
    grid-template-columns: 1fr;
  }
}
</style>
