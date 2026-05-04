<template>
  <section>
    <div class="hero">
      <div class="hero-copy">
        <span class="eyebrow">账号权益自动交付平台</span>
        <h1>更快完成购买、兑换和验证码查询</h1>
        <p>商品库存实时呈现，付款后自动生成 CDKey，账号交付与登录验证码查询都在同一个清晰流程里完成。</p>
        <div class="hero-actions">
          <a href="#products" class="primary-link">浏览商品</a>
          <RouterLink to="/verification-code" class="secondary-link">查询验证码</RouterLink>
        </div>
      </div>
      <div class="hero-media">
        <img src="https://images.unsplash.com/photo-1551434678-e076c223a692?w=1200" alt="账号购买平台" />
        <div class="live-panel">
          <span>当前可售</span>
          <strong>{{ totalStock }}</strong>
          <small>{{ products.length || 0 }} 个商品动态同步</small>
        </div>
      </div>
    </div>

    <div class="trust-strip">
      <div><strong>自动发卡</strong><span>支付确认后生成 CDKey</span></div>
      <div><strong>库存加密</strong><span>账号密码加密存储</span></div>
      <div><strong>收款码支付</strong><span>支持微信和支付宝</span></div>
      <div><strong>验证码查询</strong><span>按售出邮箱隔离匹配</span></div>
    </div>

    <div id="products" class="section-head">
      <div>
        <h2>精选商品</h2>
        <p class="muted">{{ productSummary }}</p>
      </div>
      <div class="search">
        <input v-model="keyword" placeholder="搜索商品" @keyup.enter="load" />
        <button @click="load">搜索</button>
      </div>
    </div>

    <div v-if="loading" class="grid product-grid">
      <div v-for="i in 3" :key="i" class="product-card skeleton-card">
        <span></span>
        <div class="product-body">
          <i></i>
          <b></b>
          <em></em>
        </div>
      </div>
    </div>
    <p v-else-if="error" class="muted">{{ error }}</p>
    <div v-else-if="!products.length" class="empty-state">
      <strong>暂时没有匹配商品</strong>
      <span>换个关键词试试，或稍后刷新库存。</span>
    </div>
    <div v-else class="grid product-grid">
      <RouterLink v-for="item in products" :key="item.id" class="product-card" :to="`/product/${item.id}`">
        <img :src="item.cover_url || fallback" alt="" class="cover" />
        <div class="product-body">
          <div class="row">
            <span class="status">现货 {{ item.available_stock }}</span>
            <span class="tag">自动交付</span>
          </div>
          <h3>{{ item.name }}</h3>
          <p class="muted">{{ item.subtitle }}</p>
          <div class="product-bottom">
            <span class="price">¥{{ item.price }}</span>
            <span class="buy-link">查看详情</span>
          </div>
        </div>
      </RouterLink>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api } from '../api/client'

const fallback = 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=900'
const keyword = ref('')
const loading = ref(false)
const error = ref('')
const products = ref<any[]>([])

const totalStock = computed(() => products.value.reduce((sum, item) => sum + Number(item.available_stock || 0), 0))
const minPrice = computed(() => {
  const prices = products.value.map((item) => Number(item.price)).filter((price) => !Number.isNaN(price))
  return prices.length ? Math.min(...prices) : 0
})
const productSummary = computed(() => {
  if (loading.value) return '正在同步商品、库存和价格信息。'
  if (!products.value.length) return '选择需要的账号权益，付款后进入个人中心查看交付结果。'
  return `已同步 ${products.value.length} 个商品，当前库存 ${totalStock.value} 件，最低 ¥${minPrice.value} 起。`
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res: any = await api.get('/api/products', { params: { keyword: keyword.value } })
    products.value = res.data.records
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.hero {
  min-height: 500px;
  display: grid;
  grid-template-columns: minmax(0, 1.02fr) minmax(320px, .98fr);
  align-items: center;
  gap: 34px;
  padding: 28px 0 26px;
}

.hero-copy {
  max-width: 640px;
}

.eyebrow, .tag {
  display: inline-flex;
  border-radius: 999px;
  padding: 6px 10px;
  background: #fef3c7;
  color: #92400e;
  font-size: 12px;
  font-weight: 900;
}

.hero h1 {
  margin: 18px 0 16px;
  font-size: 54px;
  line-height: 1.08;
  letter-spacing: 0;
}

.hero p {
  max-width: 560px;
  color: #475569;
  font-size: 17px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
  flex-wrap: wrap;
}

.primary-link, .secondary-link {
  border-radius: 8px;
  padding: 13px 18px;
  font-weight: 900;
  transition: transform .18s ease, box-shadow .18s ease, background .18s ease;
}

.primary-link:hover, .secondary-link:hover {
  transform: translateY(-1px);
}

.primary-link {
  color: #fff;
  background: #0f766e;
  box-shadow: 0 14px 30px rgba(15, 118, 110, .22);
}

.secondary-link {
  background: #e8eef5;
  color: #1f2937;
}

.hero-media {
  position: relative;
}

.hero-media img {
  width: 100%;
  aspect-ratio: 4 / 3.05;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 28px 70px rgba(15, 23, 42, .18);
}

.live-panel {
  position: absolute;
  right: 18px;
  bottom: 18px;
  width: min(210px, calc(100% - 36px));
  border: 1px solid rgba(255, 255, 255, .72);
  border-radius: 8px;
  background: rgba(255, 255, 255, .9);
  padding: 16px;
  box-shadow: 0 18px 42px rgba(15, 23, 42, .2);
  backdrop-filter: blur(16px);
}

.live-panel span, .live-panel small {
  display: block;
  color: #64748b;
  font-weight: 800;
}

.live-panel strong {
  display: block;
  margin: 6px 0;
  color: #0f766e;
  font-size: 38px;
  line-height: 1;
}

.trust-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin: 6px 0 30px;
}

.trust-strip div {
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #fff;
  padding: 18px;
  box-shadow: 0 10px 26px rgba(15, 23, 42, .06);
}

.trust-strip strong, .trust-strip span {
  display: block;
}

.trust-strip span {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}

.search {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto;
  gap: 10px;
  min-width: 360px;
}

.product-card {
  overflow: hidden;
  padding: 0;
  background: #fff;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  box-shadow: 0 16px 42px rgba(15, 23, 42, .08);
  transition: transform .18s ease, box-shadow .18s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 24px 56px rgba(15, 23, 42, .13);
}

.cover {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  background: #dbe3ed;
}

.product-body {
  padding: 18px;
}

.product-body h3 {
  margin: 14px 0 6px;
  font-size: 21px;
  line-height: 1.25;
}

.product-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
}

.buy-link {
  color: #0f766e;
  font-weight: 900;
}

.empty-state {
  display: grid;
  gap: 8px;
  place-items: center;
  min-height: 180px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: rgba(255, 255, 255, .72);
  color: #64748b;
}

.empty-state strong {
  color: #1f2937;
}

.skeleton-card span, .skeleton-card i, .skeleton-card b, .skeleton-card em {
  display: block;
  border-radius: 8px;
  background: linear-gradient(90deg, #edf2f7 0%, #f8fafc 45%, #edf2f7 100%);
  background-size: 220% 100%;
  animation: shimmer 1.25s ease-in-out infinite;
}

.skeleton-card span {
  aspect-ratio: 16 / 10;
}

.skeleton-card i {
  width: 36%;
  height: 22px;
}

.skeleton-card b {
  width: 72%;
  height: 26px;
  margin-top: 14px;
}

.skeleton-card em {
  width: 100%;
  height: 18px;
  margin-top: 12px;
}

@keyframes shimmer {
  0% { background-position: 120% 0; }
  100% { background-position: -120% 0; }
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .hero h1 {
    font-size: 40px;
  }

  .trust-strip {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 620px) {
  .hero {
    padding-top: 12px;
    gap: 22px;
  }

  .hero h1 {
    font-size: 34px;
  }

  .hero p {
    font-size: 15px;
  }

  .hero-actions {
    display: grid;
    grid-template-columns: 1fr;
  }

  .primary-link, .secondary-link {
    text-align: center;
  }

  .live-panel {
    position: static;
    width: 100%;
    margin-top: 10px;
  }

  .trust-strip {
    grid-template-columns: 1fr;
  }

  .search {
    min-width: 0;
    width: 100%;
    grid-template-columns: 1fr;
  }
}
</style>
