import { computed, onMounted, reactive, ref } from 'vue';
import { api } from '../api/client';
const products = ref([]);
const inventory = ref([]);
const filterProductId = ref(0);
const status = ref('');
const content = ref('');
const message = ref('');
const importErrors = ref([]);
const editingId = ref(null);
const form = reactive({
    productId: 0,
    resourceAccount: '',
    resourcePassword: '',
    status: 'AVAILABLE',
    remark: ''
});
const availableCount = computed(() => inventory.value.filter((item) => item.status === 'AVAILABLE').length);
async function load() {
    const productRes = await api.get('/admin/products');
    products.value = productRes.data.records;
    form.productId = form.productId || products.value[0]?.id || 0;
    await loadInventory();
}
async function loadInventory() {
    const params = {};
    if (filterProductId.value)
        params.productId = filterProductId.value;
    if (status.value)
        params.status = status.value;
    const inventoryRes = await api.get('/admin/inventory', { params });
    inventory.value = inventoryRes.data.records;
}
async function submit() {
    try {
        importErrors.value = [];
        const res = await api.post('/admin/inventory/import', { productId: form.productId, content: content.value });
        message.value = `导入成功 ${res.data.success} 条，失败 ${res.data.errors.length} 条`;
        importErrors.value = res.data.errors;
        content.value = '';
        await loadInventory();
    }
    catch (e) {
        message.value = e.message;
        importErrors.value = [];
    }
}
function edit(item) {
    editingId.value = item.id;
    form.productId = item.product_id;
    form.resourceAccount = item.resource_account;
    form.resourcePassword = '';
    form.status = item.status;
    form.remark = item.remark || '';
}
async function saveEdit() {
    if (!editingId.value)
        return;
    try {
        await api.put(`/admin/inventory/${editingId.value}`, form);
        message.value = '库存已更新';
        resetEdit();
        await loadInventory();
    }
    catch (e) {
        message.value = e.message;
    }
}
async function disable(id) {
    await api.post(`/admin/inventory/${id}/disable`);
    await loadInventory();
}
async function enable(id) {
    await api.post(`/admin/inventory/${id}/enable`);
    await loadInventory();
}
async function remove(id) {
    try {
        await api.delete(`/admin/inventory/${id}`);
        message.value = '库存已删除';
        await loadInventory();
    }
    catch (e) {
        message.value = e.message;
    }
}
function resetEdit() {
    editingId.value = null;
    form.resourceAccount = '';
    form.resourcePassword = '';
    form.status = 'AVAILABLE';
    form.remark = '';
}
function statusText(value) {
    return value === 'AVAILABLE' ? '可售' : value === 'DISABLED' ? '停用' : value === 'ASSIGNED' ? '已分配' : value;
}
onMounted(load);
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['form-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['filter-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['mini-stats']} */ ;
/** @type {__VLS_StyleScopedClasses['mini-stats']} */ ;
/** @type {__VLS_StyleScopedClasses['mini-stats']} */ ;
/** @type {__VLS_StyleScopedClasses['inventory-layout']} */ ;
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
    ...{ onClick: (__VLS_ctx.resetEdit) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "inventory-layout" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "panel form-panel" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
(__VLS_ctx.editingId ? '编辑库存' : '批量导入库存');
__VLS_asFunctionalElement(__VLS_intrinsicElements.select, __VLS_intrinsicElements.select)({
    value: (__VLS_ctx.form.productId),
});
for (const [item] of __VLS_getVForSourceType((__VLS_ctx.products))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
        key: (item.id),
        value: (item.id),
    });
    (item.name);
}
if (__VLS_ctx.editingId) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        placeholder: "账号邮箱",
    });
    (__VLS_ctx.form.resourceAccount);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        type: "password",
        placeholder: "账号密码，留空则不修改",
    });
    (__VLS_ctx.form.resourcePassword);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.select, __VLS_intrinsicElements.select)({
        value: (__VLS_ctx.form.status),
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
        value: "AVAILABLE",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
        value: "DISABLED",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        placeholder: "备注",
    });
    (__VLS_ctx.form.remark);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "row" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.saveEdit) },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.resetEdit) },
        ...{ class: "secondary" },
    });
}
else {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.textarea, __VLS_intrinsicElements.textarea)({
        value: (__VLS_ctx.content),
        rows: "8",
        placeholder: "account,password,remark",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.submit) },
    });
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "panel filter-panel" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.select, __VLS_intrinsicElements.select)({
    ...{ onChange: (__VLS_ctx.loadInventory) },
    value: (__VLS_ctx.filterProductId),
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
    value: (0),
});
for (const [item] of __VLS_getVForSourceType((__VLS_ctx.products))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
        key: (item.id),
        value: (item.id),
    });
    (item.name);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.select, __VLS_intrinsicElements.select)({
    ...{ onChange: (__VLS_ctx.loadInventory) },
    value: (__VLS_ctx.status),
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
    value: "",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
    value: "AVAILABLE",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
    value: "DISABLED",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
    value: "ASSIGNED",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "mini-stats" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
(__VLS_ctx.inventory.length);
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
(__VLS_ctx.availableCount);
if (__VLS_ctx.message) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "muted" },
    });
    (__VLS_ctx.message);
}
if (__VLS_ctx.importErrors.length) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "error-list" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
    for (const [item] of __VLS_getVForSourceType((__VLS_ctx.importErrors))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            key: (item),
        });
        (item);
    }
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.table, __VLS_intrinsicElements.table)({
    ...{ class: "table inventory-table" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.thead, __VLS_intrinsicElements.thead)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.tr, __VLS_intrinsicElements.tr)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.tbody, __VLS_intrinsicElements.tbody)({});
for (const [item] of __VLS_getVForSourceType((__VLS_ctx.inventory))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.tr, __VLS_intrinsicElements.tr)({
        key: (item.id),
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({});
    (item.id);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({});
    (item.product_name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({});
    (item.resource_account);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "status-pill" },
    });
    (__VLS_ctx.statusText(item.status));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({});
    (item.batch_no);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({});
    (item.remark);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({
        ...{ class: "row" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.edit(item);
            } },
        ...{ class: "secondary" },
        disabled: (item.status === 'ASSIGNED'),
    });
    if (item.status === 'AVAILABLE') {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!(item.status === 'AVAILABLE'))
                        return;
                    __VLS_ctx.disable(item.id);
                } },
            ...{ class: "secondary" },
        });
    }
    if (item.status === 'DISABLED') {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!(item.status === 'DISABLED'))
                        return;
                    __VLS_ctx.enable(item.id);
                } },
        });
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.remove(item.id);
            } },
        ...{ class: "danger" },
        disabled: (item.status === 'ASSIGNED'),
    });
}
/** @type {__VLS_StyleScopedClasses['page-head']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
/** @type {__VLS_StyleScopedClasses['inventory-layout']} */ ;
/** @type {__VLS_StyleScopedClasses['panel']} */ ;
/** @type {__VLS_StyleScopedClasses['form-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['row']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['panel']} */ ;
/** @type {__VLS_StyleScopedClasses['filter-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['mini-stats']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
/** @type {__VLS_StyleScopedClasses['error-list']} */ ;
/** @type {__VLS_StyleScopedClasses['table']} */ ;
/** @type {__VLS_StyleScopedClasses['inventory-table']} */ ;
/** @type {__VLS_StyleScopedClasses['status-pill']} */ ;
/** @type {__VLS_StyleScopedClasses['row']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['danger']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            products: products,
            inventory: inventory,
            filterProductId: filterProductId,
            status: status,
            content: content,
            message: message,
            importErrors: importErrors,
            editingId: editingId,
            form: form,
            availableCount: availableCount,
            loadInventory: loadInventory,
            submit: submit,
            edit: edit,
            saveEdit: saveEdit,
            disable: disable,
            enable: enable,
            remove: remove,
            resetEdit: resetEdit,
            statusText: statusText,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
//# sourceMappingURL=InventoryPage.vue.js.map