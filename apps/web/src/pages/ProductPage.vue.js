import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '../api/client';
import { useAuthStore } from '../stores/auth';
const fallback = 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=900';
const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const product = ref(null);
const channel = ref('ALIPAY');
const submitting = ref(false);
const message = ref('');
const payment = ref(null);
const qrSrc = computed(() => {
    const url = payment.value?.payUrl || '';
    if (!url || url.startsWith('weixin://') || url.includes('/pay/result'))
        return '';
    return url;
});
async function load() {
    try {
        const res = await api.get(`/api/products/${route.params.id}`);
        product.value = res.data;
    }
    catch (e) {
        message.value = e.message;
    }
}
async function buy() {
    if (!auth.token) {
        router.push('/login');
        return;
    }
    submitting.value = true;
    message.value = '';
    try {
        const order = await api.post('/api/orders', { productId: Number(route.params.id), quantity: 1, clientType: 'WEB' });
        const payRes = await api.post('/api/payments/create', { orderNo: order.data.orderNo, channel: channel.value });
        payment.value = { ...payRes.data, orderNo: order.data.orderNo };
        message.value = '订单已创建';
    }
    catch (e) {
        message.value = e.message;
    }
    finally {
        submitting.value = false;
    }
}
async function mockSuccess() {
    if (!payment.value)
        return;
    submitting.value = true;
    try {
        await api.post('/api/payments/mock-success', { paymentNo: payment.value.paymentNo });
        message.value = '支付成功，CDKey 已发放';
        router.push('/user');
    }
    catch (e) {
        message.value = e.message;
    }
    finally {
        submitting.value = false;
    }
}
onMounted(load);
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['media-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['buy-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['buy-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['checkout-box']} */ ;
/** @type {__VLS_StyleScopedClasses['payment-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['payment-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['qr-box']} */ ;
/** @type {__VLS_StyleScopedClasses['payment-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['payment-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-layout']} */ ;
/** @type {__VLS_StyleScopedClasses['payment-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['buy-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['buy-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['payment-meta']} */ ;
// CSS variable injection 
// CSS variable injection end 
if (__VLS_ctx.product) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
        ...{ class: "product-page" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-layout" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "media-panel" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.img)({
        src: (__VLS_ctx.product.cover_url || __VLS_ctx.fallback),
        alt: "",
        ...{ class: "hero" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "buy-panel" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "status" },
    });
    (__VLS_ctx.product.available_stock);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
    (__VLS_ctx.product.name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "muted" },
    });
    (__VLS_ctx.product.subtitle);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "price-row" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "price" },
    });
    (__VLS_ctx.product.price);
    if (__VLS_ctx.product.original_price) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "original" },
        });
        (__VLS_ctx.product.original_price);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "description" },
    });
    (__VLS_ctx.product.description);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "checkout-box" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.select, __VLS_intrinsicElements.select)({
        value: (__VLS_ctx.channel),
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
        value: "ALIPAY",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
        value: "WECHAT",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
        value: "MOCK",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.buy) },
        ...{ class: "buy-button" },
        disabled: (__VLS_ctx.submitting),
    });
    (__VLS_ctx.submitting ? '处理中...' : '确认购买');
    const __VLS_0 = {}.RouterLink;
    /** @type {[typeof __VLS_components.RouterLink, typeof __VLS_components.RouterLink, ]} */ ;
    // @ts-ignore
    const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
        ...{ class: "muted center-link" },
        to: "/redeem",
    }));
    const __VLS_2 = __VLS_1({
        ...{ class: "muted center-link" },
        to: "/redeem",
    }, ...__VLS_functionalComponentArgsRest(__VLS_1));
    __VLS_3.slots.default;
    var __VLS_3;
    if (__VLS_ctx.payment) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "payment-panel" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "eyebrow" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
        __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
            ...{ class: "muted" },
        });
        if (__VLS_ctx.qrSrc) {
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                ...{ class: "qr-box" },
            });
            __VLS_asFunctionalElement(__VLS_intrinsicElements.img)({
                src: (__VLS_ctx.qrSrc),
                alt: "收款码",
            });
        }
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "payment-meta" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
        __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        (__VLS_ctx.payment.orderNo);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
        __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        (__VLS_ctx.payment.paymentNo);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
        __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        (__VLS_ctx.payment.channel);
        if (__VLS_ctx.payment.channel === 'MOCK') {
            __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
                ...{ onClick: (__VLS_ctx.mockSuccess) },
            });
        }
    }
    if (__VLS_ctx.message) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
            ...{ class: "muted message" },
        });
        (__VLS_ctx.message);
    }
}
/** @type {__VLS_StyleScopedClasses['product-page']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-layout']} */ ;
/** @type {__VLS_StyleScopedClasses['media-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['hero']} */ ;
/** @type {__VLS_StyleScopedClasses['buy-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['status']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
/** @type {__VLS_StyleScopedClasses['price-row']} */ ;
/** @type {__VLS_StyleScopedClasses['price']} */ ;
/** @type {__VLS_StyleScopedClasses['original']} */ ;
/** @type {__VLS_StyleScopedClasses['description']} */ ;
/** @type {__VLS_StyleScopedClasses['checkout-box']} */ ;
/** @type {__VLS_StyleScopedClasses['buy-button']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
/** @type {__VLS_StyleScopedClasses['center-link']} */ ;
/** @type {__VLS_StyleScopedClasses['payment-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
/** @type {__VLS_StyleScopedClasses['qr-box']} */ ;
/** @type {__VLS_StyleScopedClasses['payment-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
/** @type {__VLS_StyleScopedClasses['message']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            fallback: fallback,
            product: product,
            channel: channel,
            submitting: submitting,
            message: message,
            payment: payment,
            qrSrc: qrSrc,
            buy: buy,
            mockSuccess: mockSuccess,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
//# sourceMappingURL=ProductPage.vue.js.map