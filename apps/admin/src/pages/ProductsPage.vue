<template>
  <section>
    <div class="page-head">
      <div>
        <h1>商品管理</h1>
        <p class="muted">维护商品展示、定价、状态和库存入口。</p>
      </div>
      <button @click="reset">新增商品</button>
    </div>

    <div class="product-editor panel">
      <div class="cover-box">
        <img :src="form.coverUrl || fallbackCover" alt="" />
        <label>
          <input type="file" accept="image/*" @change="uploadCover" />
          上传封面
        </label>
      </div>
      <div class="editor-fields">
        <div class="form-grid">
          <input v-model.trim="form.productCode" placeholder="商品编码" />
          <input v-model.trim="form.name" placeholder="商品名称" />
          <input v-model.trim="form.subtitle" placeholder="副标题" />
          <input v-model.number="form.price" type="number" placeholder="售价" />
          <input v-model.number="form.originalPrice" type="number" placeholder="划线价" />
          <input v-model.number="form.sort" type="number" placeholder="排序" />
          <select v-model="form.status">
            <option value="ON_SHELF">上架</option>
            <option value="OFF_SHELF">下架</option>
          </select>
          <select v-model="form.stockDisplayMode">
            <option value="SHOW">展示库存</option>
            <option value="HIDE">隐藏库存</option>
          </select>
          <input v-model.trim="form.coverUrl" placeholder="封面 URL" />
        </div>
        <textarea v-model="form.description" rows="3" placeholder="商品说明"></textarea>
        <div class="row">
          <button @click="save">{{ editingId ? '保存修改' : '创建商品' }}</button>
          <button class="secondary" @click="reset">重置</button>
        </div>
      </div>
    </div>

    <p v-if="message" class="muted">{{ message }}</p>
    <div class="product-grid">
      <article v-for="item in products" :key="item.id" class="product-card">
        <img :src="item.cover_url || fallbackCover" alt="" />
        <div class="product-info">
          <div class="row between">
            <span class="status-pill">{{ item.status === 'ON_SHELF' ? '上架' : '下架' }}</span>
            <strong>¥{{ item.price }}</strong>
          </div>
          <h3>{{ item.name }}</h3>
          <p>{{ item.subtitle || '账号权益商品' }}</p>
          <div class="meta-row">
            <span>{{ item.product_code }}</span>
            <span>库存 {{ item.available_stock }}</span>
          </div>
          <div class="row actions">
            <button class="secondary" @click="edit(item)">编辑</button>
            <button v-if="item.status !== 'ON_SHELF'" @click="changeStatus(item.id, 'on')">上架</button>
            <button v-else class="secondary" @click="changeStatus(item.id, 'off')">下架</button>
            <button class="danger" @click="remove(item.id)">删除</button>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import { useAdminAuthStore } from '../stores/auth'

const auth = useAdminAuthStore()
const router = useRouter()
const products = ref<any[]>([])
const message = ref('')
const editingId = ref<number | null>(null)
const fallbackCover = 'https://images.unsplash.com/photo-1551434678-e076c223a692?w=1200'
const form = reactive({
  productCode: '',
  name: '',
  subtitle: '',
  coverUrl: fallbackCover,
  price: 99,
  originalPrice: 129,
  deliveryType: 'CDKEY',
  status: 'ON_SHELF',
  description: '',
  stockDisplayMode: 'SHOW',
  sort: 0
})

async function load() {
  if (!auth.token) {
    router.push('/login')
    return
  }
  const res: any = await api.get('/admin/products')
  products.value = res.data.records
}

async function save() {
  try {
    if (editingId.value) await api.put(`/admin/products/${editingId.value}`, form)
    else await api.post('/admin/products', form)
    message.value = '保存成功'
    reset()
    await load()
  } catch (e: any) {
    message.value = e.message
  }
}

async function uploadCover(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res: any = await api.post('/admin/uploads/image', formData)
    form.coverUrl = res.data.url
    message.value = '封面上传成功'
  } catch (e: any) {
    message.value = e.message
  } finally {
    ;(event.target as HTMLInputElement).value = ''
  }
}

function edit(item: any) {
  editingId.value = item.id
  form.productCode = item.product_code
  form.name = item.name
  form.subtitle = item.subtitle || ''
  form.coverUrl = item.cover_url || fallbackCover
  form.price = Number(item.price)
  form.originalPrice = Number(item.original_price || item.price)
  form.deliveryType = item.delivery_type
  form.status = item.status
  form.description = item.description || ''
  form.stockDisplayMode = item.stock_display_mode
  form.sort = Number(item.sort || 0)
}

async function changeStatus(id: number, action: 'on' | 'off') {
  await api.post(`/admin/products/${id}/${action === 'on' ? 'on-shelf' : 'off-shelf'}`)
  await load()
}

async function remove(id: number) {
  try {
    await api.delete(`/admin/products/${id}`)
    message.value = '商品已删除'
    if (editingId.value === id) reset()
    await load()
  } catch (e: any) {
    message.value = e.message
  }
}

function reset() {
  editingId.value = null
  form.productCode = ''
  form.name = ''
  form.subtitle = ''
  form.description = ''
  form.coverUrl = fallbackCover
  form.price = 99
  form.originalPrice = 129
  form.status = 'ON_SHELF'
  form.stockDisplayMode = 'SHOW'
  form.sort = 0
}

onMounted(load)
</script>

<style scoped>
.product-editor {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 18px;
  margin-bottom: 18px;
}

.cover-box {
  display: grid;
  gap: 10px;
  align-content: start;
}

.cover-box img {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #dfe8ed;
}

.cover-box label {
  display: grid;
  place-items: center;
  border-radius: 8px;
  padding: 10px;
  background: #e8eef2;
  color: #1d3139;
  font-weight: 800;
  cursor: pointer;
}

.cover-box input {
  display: none;
}

.editor-fields {
  display: grid;
  gap: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}

.product-card {
  background: #fff;
  border: 1px solid #dfe8ed;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(29, 49, 57, .07);
}

.product-card > img {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  background: #edf3f5;
}

.product-info {
  display: grid;
  gap: 10px;
  padding: 14px;
}

.product-info h3, .product-info p {
  margin: 0;
}

.product-info p, .meta-row {
  color: #667982;
}

.between, .meta-row {
  justify-content: space-between;
}

.meta-row {
  display: flex;
  gap: 12px;
  font-size: 13px;
}

.actions {
  margin-top: 2px;
}

@media (max-width: 920px) {
  .product-editor, .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
