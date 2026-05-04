import { onMounted, reactive, ref } from 'vue';
import { api } from '../api/client';
const config = ref({});
const message = ref('');
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
});
async function load() {
    try {
        const res = await api.get('/admin/payment-config');
        config.value = res.data;
        form.alipayGateway = res.data.alipay?.gateway || form.alipayGateway;
        form.alipayQrUrl = res.data.alipay?.qrUrl || '';
        form.wechatQrUrl = res.data.wechat?.qrUrl || '';
        form.wechatNotifyUrl = res.data.wechat?.notifyUrl || '';
    }
    catch (e) {
        message.value = e.message;
    }
}
async function save() {
    try {
        const res = await api.post('/admin/payment-config', form);
        config.value = res.data.config;
        form.alipayPrivateKey = '';
        form.wechatApiV3Key = '';
        message.value = '支付配置已保存';
    }
    catch (e) {
        message.value = e.message;
    }
}
async function uploadQr(event, channel) {
    const file = event.target.files?.[0];
    if (!file)
        return;
    const formData = new FormData();
    formData.append('file', file);
    try {
        const res = await api.post('/admin/uploads/image', formData);
        if (channel === 'alipay')
            form.alipayQrUrl = res.data.url;
        else
            form.wechatQrUrl = res.data.url;
        await save();
        message.value = '收款码已上传';
    }
    catch (e) {
        message.value = e.message;
    }
    finally {
        ;
        event.target.value = '';
    }
}
onMounted(load);
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['form-card']} */ ;
/** @type {__VLS_StyleScopedClasses['upload-box']} */ ;
/** @type {__VLS_StyleScopedClasses['upload-box']} */ ;
/** @type {__VLS_StyleScopedClasses['pay-status']} */ ;
/** @type {__VLS_StyleScopedClasses['config-grid']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "page-head" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.save) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "config-grid" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "panel form-card" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "支付宝 AppId",
});
(__VLS_ctx.form.alipayAppId);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "支付宝商户号 / SellerId",
});
(__VLS_ctx.form.alipayMerchantId);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "支付宝网关",
});
(__VLS_ctx.form.alipayGateway);
__VLS_asFunctionalElement(__VLS_intrinsicElements.textarea, __VLS_intrinsicElements.textarea)({
    value: (__VLS_ctx.form.alipayPrivateKey),
    rows: "3",
    placeholder: "支付宝私钥，留空则不修改",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
    ...{ class: "upload-box" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    ...{ onChange: (...[$event]) => {
            __VLS_ctx.uploadQr($event, 'alipay');
        } },
    type: "file",
    accept: "image/*",
});
if (__VLS_ctx.form.alipayQrUrl) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.img)({
        src: (__VLS_ctx.form.alipayQrUrl),
        alt: "支付宝收款码",
    });
}
else {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "panel form-card" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "微信 AppId",
});
(__VLS_ctx.form.wechatAppId);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "微信商户号 MchId",
});
(__VLS_ctx.form.wechatMchId);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "微信回调地址",
});
(__VLS_ctx.form.wechatNotifyUrl);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "password",
    placeholder: "API v3 Key，留空则不修改",
});
(__VLS_ctx.form.wechatApiV3Key);
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
    ...{ class: "upload-box" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    ...{ onChange: (...[$event]) => {
            __VLS_ctx.uploadQr($event, 'wechat');
        } },
    type: "file",
    accept: "image/*",
});
if (__VLS_ctx.form.wechatQrUrl) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.img)({
        src: (__VLS_ctx.form.wechatQrUrl),
        alt: "微信收款码",
    });
}
else {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
}
if (__VLS_ctx.message) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "muted" },
    });
    (__VLS_ctx.message);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "status-grid" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "card pay-status" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
(__VLS_ctx.config.alipay?.ready || __VLS_ctx.config.alipay?.qrEnabled ? '可用' : '待配置');
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "card pay-status" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
(__VLS_ctx.config.wechat?.ready || __VLS_ctx.config.wechat?.qrEnabled ? '可用' : '待配置');
/** @type {__VLS_StyleScopedClasses['page-head']} */ ;
/** @type {__VLS_StyleScopedClasses['config-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['panel']} */ ;
/** @type {__VLS_StyleScopedClasses['form-card']} */ ;
/** @type {__VLS_StyleScopedClasses['upload-box']} */ ;
/** @type {__VLS_StyleScopedClasses['panel']} */ ;
/** @type {__VLS_StyleScopedClasses['form-card']} */ ;
/** @type {__VLS_StyleScopedClasses['upload-box']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
/** @type {__VLS_StyleScopedClasses['status-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['card']} */ ;
/** @type {__VLS_StyleScopedClasses['pay-status']} */ ;
/** @type {__VLS_StyleScopedClasses['card']} */ ;
/** @type {__VLS_StyleScopedClasses['pay-status']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            config: config,
            message: message,
            form: form,
            save: save,
            uploadQr: uploadQr,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
//# sourceMappingURL=PaymentConfigPage.vue.js.map