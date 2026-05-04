import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../api/client';
import { useAdminAuthStore } from '../stores/auth';
const auth = useAdminAuthStore();
const router = useRouter();
const products = ref([]);
const message = ref('');
const editingId = ref(null);
const fallbackCover = 'https://images.unsplash.com/photo-1551434678-e076c223a692?w=1200';
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
});
async function load() {
    if (!auth.token) {
        router.push('/login');
        return;
    }
    const res = await api.get('/admin/products');
    products.value = res.data.records;
}
async function save() {
    try {
        if (editingId.value)
            await api.put(`/admin/products/${editingId.value}`, form);
        else
            await api.post('/admin/products', form);
        message.value = '保存成功';
        reset();
        await load();
    }
    catch (e) {
        message.value = e.message;
    }
}
async function uploadCover(event) {
    const file = event.target.files?.[0];
    if (!file)
        return;
    const formData = new FormData();
    formData.append('file', file);
    try {
        const res = await api.post('/admin/uploads/image', formData);
        form.coverUrl = res.data.url;
        message.value = '封面上传成功';
    }
    catch (e) {
        message.value = e.message;
    }
    finally {
        ;
        event.target.value = '';
    }
}
function edit(item) {
    editingId.value = item.id;
    form.productCode = item.product_code;
    form.name = item.name;
    form.subtitle = item.subtitle || '';
    form.coverUrl = item.cover_url || fallbackCover;
    form.price = Number(item.price);
    form.originalPrice = Number(item.original_price || item.price);
    form.deliveryType = item.delivery_type;
    form.status = item.status;
    form.description = item.description || '';
    form.stockDisplayMode = item.stock_display_mode;
    form.sort = Number(item.sort || 0);
}
async function changeStatus(id, action) {
    await api.post(`/admin/products/${id}/${action === 'on' ? 'on-shelf' : 'off-shelf'}`);
    await load();
}
async function remove(id) {
    try {
        await api.delete(`/admin/products/${id}`);
        message.value = '商品已删除';
        if (editingId.value === id)
            reset();
        await load();
    }
    catch (e) {
        message.value = e.message;
    }
}
function reset() {
    editingId.value = null;
    form.productCode = '';
    form.name = '';
    form.subtitle = '';
    form.description = '';
    form.coverUrl = fallbackCover;
    form.price = 99;
    form.originalPrice = 129;
    form.status = 'ON_SHELF';
    form.stockDisplayMode = 'SHOW';
    form.sort = 0;
}
onMounted(load);
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['cover-box']} */ ;
/** @type {__VLS_StyleScopedClasses['cover-box']} */ ;
/** @type {__VLS_StyleScopedClasses['cover-box']} */ ;
/** @type {__VLS_StyleScopedClasses['product-card']} */ ;
/** @type {__VLS_StyleScopedClasses['product-info']} */ ;
/** @type {__VLS_StyleScopedClasses['product-info']} */ ;
/** @type {__VLS_StyleScopedClasses['product-info']} */ ;
/** @type {__VLS_StyleScopedClasses['meta-row']} */ ;
/** @type {__VLS_StyleScopedClasses['meta-row']} */ ;
/** @type {__VLS_StyleScopedClasses['product-editor']} */ ;
/** @type {__VLS_StyleScopedClasses['form-grid']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "page-head" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "muted" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.reset) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "product-editor panel" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "cover-box" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.img)({
    src: (__VLS_ctx.form.coverUrl || __VLS_ctx.fallbackCover),
    alt: "",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    ...{ onChange: (__VLS_ctx.uploadCover) },
    type: "file",
    accept: "image/*",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "editor-fields" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "form-grid" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "商品编码",
});
(__VLS_ctx.form.productCode);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "商品名称",
});
(__VLS_ctx.form.name);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "副标题",
});
(__VLS_ctx.form.subtitle);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "number",
    placeholder: "售价",
});
(__VLS_ctx.form.price);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "number",
    placeholder: "划线价",
});
(__VLS_ctx.form.originalPrice);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "number",
    placeholder: "排序",
});
(__VLS_ctx.form.sort);
__VLS_asFunctionalElement(__VLS_intrinsicElements.select, __VLS_intrinsicElements.select)({
    value: (__VLS_ctx.form.status),
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
    value: "ON_SHELF",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
    value: "OFF_SHELF",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.select, __VLS_intrinsicElements.select)({
    value: (__VLS_ctx.form.stockDisplayMode),
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
    value: "SHOW",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
    value: "HIDE",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "封面 URL",
});
(__VLS_ctx.form.coverUrl);
__VLS_asFunctionalElement(__VLS_intrinsicElements.textarea, __VLS_intrinsicElements.textarea)({
    value: (__VLS_ctx.form.description),
    rows: "3",
    placeholder: "商品说明",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "row" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.save) },
});
(__VLS_ctx.editingId ? '保存修改' : '创建商品');
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.reset) },
    ...{ class: "secondary" },
});
if (__VLS_ctx.message) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "muted" },
    });
    (__VLS_ctx.message);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "product-grid" },
});
for (const [item] of __VLS_getVForSourceType((__VLS_ctx.products))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.article, __VLS_intrinsicElements.article)({
        key: (item.id),
        ...{ class: "product-card" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.img)({
        src: (item.cover_url || __VLS_ctx.fallbackCover),
        alt: "",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "product-info" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "row between" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "status-pill" },
    });
    (item.status === 'ON_SHELF' ? '上架' : '下架');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
    (item.price);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
    (item.name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
    (item.subtitle || '账号权益商品');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "meta-row" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    (item.product_code);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    (item.available_stock);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "row actions" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.edit(item);
            } },
        ...{ class: "secondary" },
    });
    if (item.status !== 'ON_SHELF') {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!(item.status !== 'ON_SHELF'))
                        return;
                    __VLS_ctx.changeStatus(item.id, 'on');
                } },
        });
    }
    else {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(item.status !== 'ON_SHELF'))
                        return;
                    __VLS_ctx.changeStatus(item.id, 'off');
                } },
            ...{ class: "secondary" },
        });
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.remove(item.id);
            } },
        ...{ class: "danger" },
    });
}
/** @type {__VLS_StyleScopedClasses['page-head']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
/** @type {__VLS_StyleScopedClasses['product-editor']} */ ;
/** @type {__VLS_StyleScopedClasses['panel']} */ ;
/** @type {__VLS_StyleScopedClasses['cover-box']} */ ;
/** @type {__VLS_StyleScopedClasses['editor-fields']} */ ;
/** @type {__VLS_StyleScopedClasses['form-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['row']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
/** @type {__VLS_StyleScopedClasses['product-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['product-card']} */ ;
/** @type {__VLS_StyleScopedClasses['product-info']} */ ;
/** @type {__VLS_StyleScopedClasses['row']} */ ;
/** @type {__VLS_StyleScopedClasses['between']} */ ;
/** @type {__VLS_StyleScopedClasses['status-pill']} */ ;
/** @type {__VLS_StyleScopedClasses['meta-row']} */ ;
/** @type {__VLS_StyleScopedClasses['row']} */ ;
/** @type {__VLS_StyleScopedClasses['actions']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['danger']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            products: products,
            message: message,
            editingId: editingId,
            fallbackCover: fallbackCover,
            form: form,
            save: save,
            uploadCover: uploadCover,
            edit: edit,
            changeStatus: changeStatus,
            remove: remove,
            reset: reset,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
//# sourceMappingURL=ProductsPage.vue.js.map