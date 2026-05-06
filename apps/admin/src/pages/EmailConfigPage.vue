<template>
  <section>
    <h1>Google 邮箱配置</h1>
    <div class="panel form">
      <input v-model.trim="form.username" placeholder="Google 邮箱，例如 example@gmail.com" />
      <input v-model="form.appPassword" type="password" placeholder="邮箱应用密钥，留空则不修改" />
      <input v-model.trim="form.host" placeholder="IMAP Host，默认 imap.gmail.com" />
      <label class="check-row">
        <input v-model="form.imapProxyEnabled" type="checkbox" />
        <span>启用 IMAP 代理</span>
      </label>
      <input v-model.trim="form.imapProxyUrl" placeholder="IMAP 代理地址，例如 socks5://host:port" />
      <input v-model.trim="form.smtpHost" placeholder="SMTP Host，默认 smtp.gmail.com" />
      <label class="check-row">
        <input v-model="form.smtpProxyEnabled" type="checkbox" />
        <span>启用 SMTP 代理</span>
      </label>
      <input v-model.trim="form.smtpProxyUrl" placeholder="SMTP 代理地址，例如 socks5://host:port" />
      <p class="hint">注册邮件可用 Gmail SMTP，也可在线上通过 Render 环境变量配置 Resend 走 HTTPS 发信。</p>
      <input v-model.trim="form.folder" placeholder="邮件文件夹，默认 INBOX" />
      <button @click="save">保存配置</button>
    </div>
    <div class="card status">
      <h3>当前状态</h3>
      <p>邮箱：{{ current.username || '未配置' }}</p>
      <p>IMAP：{{ current.host }}</p>
      <p>IMAP 代理：{{ proxyText }}</p>
      <p>SMTP：{{ current.smtpHost }}:587</p>
      <p>SMTP 代理：{{ smtpProxyText }}</p>
      <p>Folder：{{ current.folder }}</p>
      <p>应用密钥：{{ current.hasAppPassword ? '已配置' : '未配置' }}</p>
      <p>就绪：{{ current.ready ? '是' : '否' }}</p>
    </div>
    <p v-if="message" class="muted">{{ message }}</p>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { api } from '../api/client'

const form = reactive({
  username: '',
  appPassword: '',
  host: 'imap.gmail.com',
  imapProxyEnabled: false,
  imapProxyUrl: '',
  smtpHost: 'smtp.gmail.com',
  smtpProxyEnabled: false,
  smtpProxyUrl: '',
  folder: 'INBOX'
})
const current = ref<any>({})
const message = ref('')
const proxyText = computed(() => {
  const enabled = current.value.imapProxyEnabled === true || current.value.imapProxyEnabled === 'true'
  return enabled ? (current.value.imapProxyUrl || '未配置') : '未启用'
})
const smtpProxyText = computed(() => {
  const enabled = current.value.smtpProxyEnabled === true || current.value.smtpProxyEnabled === 'true'
  return enabled ? (current.value.smtpProxyUrl || '未配置') : '未启用'
})

async function load() {
  try {
    const res: any = await api.get('/admin/email-config')
    current.value = res.data
    form.username = res.data.username || ''
    form.host = res.data.host || 'imap.gmail.com'
    form.imapProxyEnabled = res.data.imapProxyEnabled === true || res.data.imapProxyEnabled === 'true'
    form.imapProxyUrl = res.data.imapProxyUrl || ''
    form.smtpHost = res.data.smtpHost || 'smtp.gmail.com'
    form.smtpProxyEnabled = res.data.smtpProxyEnabled === true || res.data.smtpProxyEnabled === 'true'
    form.smtpProxyUrl = res.data.smtpProxyUrl || ''
    form.folder = res.data.folder || 'INBOX'
  } catch (e: any) {
    message.value = e.message
  }
}

async function save() {
  try {
    await api.post('/admin/email-config', form)
    form.appPassword = ''
    message.value = '保存成功'
    await load()
  } catch (e: any) {
    message.value = e.message
  }
}

onMounted(load)
</script>

<style scoped>
.form {
  display: grid;
  gap: 10px;
  max-width: 540px;
}

.check-row {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #1d3139;
  font-weight: 800;
}

.check-row input {
  width: auto;
}

.status {
  margin-top: 16px;
  max-width: 540px;
}

.hint {
  margin: -4px 0 2px;
  color: #5f7077;
  font-size: 13px;
}
</style>
