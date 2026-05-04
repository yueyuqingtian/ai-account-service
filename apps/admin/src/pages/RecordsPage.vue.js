import { computed, onMounted, ref, watch } from 'vue';
import { api } from '../api/client';
const tab = ref('orders');
const rows = ref([]);
const message = ref('');
const keyword = ref('');
const keys = computed(() => {
    if (tab.value === 'orders')
        return ['order_no', 'user_id', 'pay_amount', 'pay_status', 'delivery_status', 'created_at'];
    if (tab.value === 'payments')
        return ['payment_no', 'order_no', 'channel', 'amount', 'status', 'paid_at'];
    if (tab.value === 'cdkeys')
        return ['cdk_code', 'owner_user_id', 'order_no', 'status', 'created_at'];
    if (tab.value === 'redeems')
        return ['redeem_no', 'user_id', 'cdk_code', 'result', 'fail_reason'];
    if (tab.value === 'verification-logs')
        return ['user_id', 'resource_account', 'result', 'code_value', 'fail_reason', 'created_at'];
    return ['admin_id', 'module', 'operation_type', 'target_id', 'created_at'];
});
const labelMap = {
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
};
const columns = computed(() => keys.value.map((key) => labelMap[key] || key));
async function load() {
    try {
        const res = await api.get(`/admin/${tab.value}`, { params: { keyword: keyword.value } });
        rows.value = res.data.records;
    }
    catch (e) {
        message.value = e.message;
    }
}
async function markPaid(orderNo) {
    try {
        await api.post(`/admin/orders/${orderNo}/mark-paid`);
        message.value = '收款已确认';
        await load();
    }
    catch (e) {
        message.value = e.message;
    }
}
watch(tab, load);
onMounted(load);
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "row" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.tab = 'orders';
        } },
    ...{ class: "secondary" },
    ...{ class: ({ active: __VLS_ctx.tab === 'orders' }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.tab = 'payments';
        } },
    ...{ class: "secondary" },
    ...{ class: ({ active: __VLS_ctx.tab === 'payments' }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.tab = 'cdkeys';
        } },
    ...{ class: "secondary" },
    ...{ class: ({ active: __VLS_ctx.tab === 'cdkeys' }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.tab = 'redeems';
        } },
    ...{ class: "secondary" },
    ...{ class: ({ active: __VLS_ctx.tab === 'redeems' }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.tab = 'logs';
        } },
    ...{ class: "secondary" },
    ...{ class: ({ active: __VLS_ctx.tab === 'logs' }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.tab = 'verification-logs';
        } },
    ...{ class: "secondary" },
    ...{ class: ({ active: __VLS_ctx.tab === 'verification-logs' }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    ...{ onKeyup: (__VLS_ctx.load) },
    placeholder: "搜索关键字",
});
(__VLS_ctx.keyword);
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.load) },
});
if (__VLS_ctx.message) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "muted" },
    });
    (__VLS_ctx.message);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.table, __VLS_intrinsicElements.table)({
    ...{ class: "table records-table" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.thead, __VLS_intrinsicElements.thead)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.tr, __VLS_intrinsicElements.tr)({});
for (const [col] of __VLS_getVForSourceType((__VLS_ctx.columns))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({
        key: (col),
    });
    (col);
}
if (__VLS_ctx.tab === 'orders') {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.tbody, __VLS_intrinsicElements.tbody)({});
for (const [row] of __VLS_getVForSourceType((__VLS_ctx.rows))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.tr, __VLS_intrinsicElements.tr)({
        key: (row.id),
    });
    for (const [col] of __VLS_getVForSourceType((__VLS_ctx.keys))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({
            key: (col),
        });
        (row[col]);
    }
    if (__VLS_ctx.tab === 'orders') {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({});
        if (row.pay_status === 'UNPAID') {
            __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
                ...{ onClick: (...[$event]) => {
                        if (!(__VLS_ctx.tab === 'orders'))
                            return;
                        if (!(row.pay_status === 'UNPAID'))
                            return;
                        __VLS_ctx.markPaid(row.order_no);
                    } },
                ...{ class: "secondary" },
            });
        }
        else {
            __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
                ...{ class: "status-pill" },
            });
        }
    }
}
/** @type {__VLS_StyleScopedClasses['row']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
/** @type {__VLS_StyleScopedClasses['table']} */ ;
/** @type {__VLS_StyleScopedClasses['records-table']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['status-pill']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            tab: tab,
            rows: rows,
            message: message,
            keyword: keyword,
            keys: keys,
            columns: columns,
            load: load,
            markPaid: markPaid,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
//# sourceMappingURL=RecordsPage.vue.js.map