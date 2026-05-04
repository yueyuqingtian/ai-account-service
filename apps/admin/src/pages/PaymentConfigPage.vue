<template>
  <section>
    <div class="page-head">
      <div>
        <h1>支付配置</h1>
      </div>
      <button @click="save">保存配置</button>
    </div>

    <div class="config-grid">
      <div class="panel form-card">
        <h3>支付宝</h3>
        <input v-model.trim="form.alipayAppId" placeholder="支付宝 AppId" />
        <input v-model.trim="form.alipayMerchantId" placeholder="支付宝商户号 / SellerId" />
        <input v-model.trim="form.alipayGateway" placeholder="支付宝网关" />
        <textarea v-model="form.alipayPrivateKey" rows="3" placeholder="支付宝私钥，留空则不修改"></textarea>
        <label class="upload-box">
          <input type="file" accept="image/*" @change="uploadQr($event, 'alipay')" />
          <img v-if="form.alipayQrUrl" :src="form.alipayQrUrl" alt="支付宝收款码" />
          <span v-else>上传支付宝收款码</span>
        </label>
      </div>

      <div class="panel form-card">
        <h3>微信支付</h3>
        <input v-model.trim="form.wechatAppId" placeholder="微信 AppId" />
        <input v-model.trim="form.wechatMchId" placeholder="微信商户号 MchId" />
        <input v-model.trim="form.wechatNotifyUrl" placeholder="微信回调地址" />
        <input v-model="form.wechatApiV3Key" type="password" placeholder="API v3 Key，留空则不修改" />
        <label class="upload-box">
          <input type="file" accept="image/*" @change="uploadQr($event, 'wechat')" />
          <img v-if="form.wechatQrUrl" :src="form.wechatQrUrl" alt="微信收款码" />
          <span v-else>上传微信收款码</span>
        </label>
      </div>
    </div>

    <p v-if="message" class="muted">{{ message }}</p>
    <div class="status-grid">
      <div class="card pay-status">
        <span>支付宝</span>
        <strong>{{ config.alipay?.ready || config.alipay?.qrEnabled ? '可用' : '待配置' }}</strong>
      </div>
      <div class="card pay-status">
        <span>微信支付</span>
        <strong>{{ config.wechat?.ready || config.wechat?.qrEnabled ? '可用' : '待配置' }}</strong>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api/client'

const config = ref<Record<string, any>>({})
const message = ref('')
const form = reactive({
  alipayAppId: '',
  alipayMerchantId: '',
  alipayPrivateKey: '',
  alipayGateway: 'https://openapi.alipay.com/gateway.do',
  alipayQrUrl: '',
  wechatAppId: '',
  wechatMchId: '',
  wechatApiV3Key: '',
  wechatNotifyUrl: '',
  wechatQrUrl: ''
})

async function load() {
  try {
    const res: any = await api.get('/admin/payment-config')
    config.value = res.data
    form.alipayGateway = res.data.alipay?.gateway || form.alipayGateway
    form.alipayQrUrl = res.data.alipay?.qrUrl || ''
    form.wechatQrUrl = res.data.wechat?.qrUrl || ''
    form.wechatNotifyUrl = res.data.wechat?.notifyUrl || ''
  } catch (e: any) {
    message.value = e.message
  }
}

async function save() {
  try {
    const res: any = await api.post('/admin/payment-config', form)
    config.value = res.data.config
    form.alipayPrivateKey = ''
    form.wechatApiV3Key = ''
    message.value = '支付配置已保存'
  } catch (e: any) {
    message.value = e.message
  }
}

async function uploadQr(event: Event, channel: 'alipay' | 'wechat') {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res: any = await api.post('/admin/uploads/image', formData)
    if (channel === 'alipay') form.alipayQrUrl = res.data.url
    else form.wechatQrUrl = res.data.url
    await save()
    message.value = '收款码已上传'
  } catch (e: any) {
    message.value = e.message
  } finally {
    ;(event.target as HTMLInputElement).value = ''
  }
}

onMounted(load)
</script>

<style scoped>
.config-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.form-card {
  display: grid;
  gap: 10px;
}

.form-card h3 {
  margin: 0 0 4px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.upload-box {
  min-height: 220px;
  border: 1px dashed #aebdc5;
  border-radius: 8px;
  background: #f8fbfc;
  display: grid;
  place-items: center;
  cursor: pointer;
  overflow: hidden;
  color: #5d7079;
  font-weight: 800;
}

.upload-box input {
  display: none;
}

.upload-box img {
  width: 100%;
  height: 100%;
  max-height: 280px;
  object-fit: contain;
  padding: 10px;
}

.pay-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pay-status strong {
  color: #1f6f8b;
}

@media (max-width: 900px) {
  .config-grid {
    grid-template-columns: 1fr;
  }
}
</style>
