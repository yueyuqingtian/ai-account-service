<template>
  <section>
    <div class="page-head">
      <div>
        <h1>库存管理</h1>
        <p class="muted">维护账号库存、批量导入、编辑备注和下架不可用账号。</p>
      </div>
      <button @click="resetEdit">新增/导入</button>
    </div>

    <div class="inventory-layout">
      <div class="panel form-panel">
        <h3>{{ editingId ? '编辑库存' : '批量导入库存' }}</h3>
        <select v-model.number="form.productId">
          <option v-for="item in products" :key="item.id" :value="item.id">{{ item.name }}</option>
        </select>

        <template v-if="editingId">
          <input v-model.trim="form.resourceAccount" placeholder="账号邮箱" />
          <input v-model="form.resourcePassword" type="password" placeholder="账号密码，留空则不修改" />
          <select v-model="form.status">
            <option value="AVAILABLE">可售</option>
            <option value="DISABLED">停用</option>
          </select>
          <input v-model.trim="form.remark" placeholder="备注" />
          <div class="row">
            <button @click="saveEdit">保存修改</button>
            <button class="secondary" @click="resetEdit">取消</button>
          </div>
        </template>

        <template v-else>
          <textarea v-model="content" rows="8" placeholder="account,password,remark"></textarea>
          <button @click="submit">导入库存</button>
        </template>
      </div>

      <div class="panel filter-panel">
        <h3>筛选</h3>
        <select v-model.number="filterProductId" @change="loadInventory">
          <option :value="0">全部商品</option>
          <option v-for="item in products" :key="item.id" :value="item.id">{{ item.name }}</option>
        </select>
        <select v-model="status" @change="loadInventory">
          <option value="">全部状态</option>
          <option value="AVAILABLE">可售</option>
          <option value="DISABLED">停用</option>
          <option value="ASSIGNED">已分配</option>
        </select>
        <div class="mini-stats">
          <div><span>总数</span><strong>{{ inventory.length }}</strong></div>
          <div><span>可售</span><strong>{{ availableCount }}</strong></div>
        </div>
      </div>
    </div>

    <p v-if="message" class="muted">{{ message }}</p>
    <div v-if="importErrors.length" class="error-list">
      <strong>失败明细</strong>
      <span v-for="item in importErrors" :key="item">{{ item }}</span>
    </div>
    <table class="table inventory-table">
      <thead>
        <tr>
          <th>ID</th><th>商品</th><th>账号</th><th>状态</th><th>批次</th><th>备注</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in inventory" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.product_name }}</td>
          <td>{{ item.resource_account }}</td>
          <td><span class="status-pill">{{ statusText(item.status) }}</span></td>
          <td>{{ item.batch_no }}</td>
          <td>{{ item.remark }}</td>
          <td class="row">
            <button class="secondary" :disabled="item.status === 'ASSIGNED'" @click="edit(item)">编辑</button>
            <button v-if="item.status === 'AVAILABLE'" class="secondary" @click="disable(item.id)">停用</button>
            <button v-if="item.status === 'DISABLED'" @click="enable(item.id)">启用</button>
            <button class="danger" :disabled="item.status === 'ASSIGNED'" @click="remove(item.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { api } from '../api/client'

const products = ref<any[]>([])
const inventory = ref<any[]>([])
const filterProductId = ref(0)
const status = ref('')
const content = ref('')
const message = ref('')
const importErrors = ref<string[]>([])
const editingId = ref<number | null>(null)
const form = reactive({
  productId: 0,
  resourceAccount: '',
  resourcePassword: '',
  status: 'AVAILABLE',
  remark: ''
})
const availableCount = computed(() => inventory.value.filter((item) => item.status === 'AVAILABLE').length)

async function load() {
  const productRes: any = await api.get('/admin/products')
  products.value = productRes.data.records
  form.productId = form.productId || products.value[0]?.id || 0
  await loadInventory()
}

async function loadInventory() {
  const params: Record<string, any> = {}
  if (filterProductId.value) params.productId = filterProductId.value
  if (status.value) params.status = status.value
  const inventoryRes: any = await api.get('/admin/inventory', { params })
  inventory.value = inventoryRes.data.records
}

async function submit() {
  try {
    importErrors.value = []
    const res: any = await api.post('/admin/inventory/import', { productId: form.productId, content: content.value })
    message.value = `导入成功 ${res.data.success} 条，失败 ${res.data.errors.length} 条`
    importErrors.value = res.data.errors
    content.value = ''
    await loadInventory()
  } catch (e: any) {
    message.value = e.message
    importErrors.value = []
  }
}

function edit(item: any) {
  editingId.value = item.id
  form.productId = item.product_id
  form.resourceAccount = item.resource_account
  form.resourcePassword = ''
  form.status = item.status
  form.remark = item.remark || ''
}

async function saveEdit() {
  if (!editingId.value) return
  try {
    await api.put(`/admin/inventory/${editingId.value}`, form)
    message.value = '库存已更新'
    resetEdit()
    await loadInventory()
  } catch (e: any) {
    message.value = e.message
  }
}

async function disable(id: number) {
  await api.post(`/admin/inventory/${id}/disable`)
  await loadInventory()
}

async function enable(id: number) {
  await api.post(`/admin/inventory/${id}/enable`)
  await loadInventory()
}

async function remove(id: number) {
  try {
    await api.delete(`/admin/inventory/${id}`)
    message.value = '库存已删除'
    await loadInventory()
  } catch (e: any) {
    message.value = e.message
  }
}

function resetEdit() {
  editingId.value = null
  form.resourceAccount = ''
  form.resourcePassword = ''
  form.status = 'AVAILABLE'
  form.remark = ''
}

function statusText(value: string) {
  return value === 'AVAILABLE' ? '可售' : value === 'DISABLED' ? '停用' : value === 'ASSIGNED' ? '已分配' : value
}

onMounted(load)
</script>

<style scoped>
.inventory-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  margin-bottom: 16px;
}

.form-panel, .filter-panel {
  display: grid;
  gap: 12px;
}

.form-panel h3, .filter-panel h3 {
  margin: 0;
}

.mini-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.mini-stats div {
  border: 1px solid #e1e9ee;
  border-radius: 8px;
  padding: 12px;
  background: #f8fbfc;
}

.mini-stats span {
  display: block;
  color: #667982;
  font-size: 12px;
}

.mini-stats strong {
  display: block;
  margin-top: 4px;
  font-size: 24px;
  color: #1f6f8b;
}

.inventory-table {
  margin-top: 16px;
}

.error-list {
  display: grid;
  gap: 6px;
  margin: 10px 0 16px;
  border: 1px solid #fecdd3;
  border-radius: 8px;
  background: #fff1f2;
  color: #be123c;
  padding: 12px;
}

@media (max-width: 980px) {
  .inventory-layout {
    grid-template-columns: 1fr;
  }
}
</style>
