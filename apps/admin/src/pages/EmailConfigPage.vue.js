import { computed, onMounted, reactive, ref } from 'vue';
import { api } from '../api/client';
const form = reactive({
    username: '',
    appPassword: '',
    host: 'imap.gmail.com',
    imapProxyEnabled: true,
    imapProxyUrl: 'http://127.0.0.1:7897',
    smtpHost: 'smtp.gmail.com',
    smtpProxyEnabled: true,
    smtpProxyUrl: 'http://127.0.0.1:7897',
    folder: 'INBOX'
});
const current = ref({});
const message = ref('');
const proxyText = computed(() => {
    const enabled = current.value.imapProxyEnabled === true || current.value.imapProxyEnabled === 'true';
    return enabled ? (current.value.imapProxyUrl || 'http://127.0.0.1:7897') : '未启用';
});
const smtpProxyText = computed(() => {
    const enabled = current.value.smtpProxyEnabled === true || current.value.smtpProxyEnabled === 'true';
    return enabled ? (current.value.smtpProxyUrl || current.value.imapProxyUrl || 'http://127.0.0.1:7897') : '未启用';
});
async function load() {
    try {
        const res = await api.get('/admin/email-config');
        current.value = res.data;
        form.username = res.data.username || '';
        form.host = res.data.host || 'imap.gmail.com';
        form.imapProxyEnabled = res.data.imapProxyEnabled === true || res.data.imapProxyEnabled === 'true';
        form.imapProxyUrl = res.data.imapProxyUrl || 'http://127.0.0.1:7897';
        form.smtpHost = res.data.smtpHost || 'smtp.gmail.com';
        form.smtpProxyEnabled = res.data.smtpProxyEnabled === true || res.data.smtpProxyEnabled === 'true';
        form.smtpProxyUrl = res.data.smtpProxyUrl || form.imapProxyUrl;
        form.folder = res.data.folder || 'INBOX';
    }
    catch (e) {
        message.value = e.message;
    }
}
async function save() {
    try {
        await api.post('/admin/email-config', form);
        form.appPassword = '';
        message.value = '保存成功';
        await load();
    }
    catch (e) {
        message.value = e.message;
    }
}
onMounted(load);
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['check-row']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "panel form" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "Google 邮箱，例如 example@gmail.com",
});
(__VLS_ctx.form.username);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "password",
    placeholder: "邮箱应用密钥，留空则不修改",
});
(__VLS_ctx.form.appPassword);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "IMAP Host，默认 imap.gmail.com",
});
(__VLS_ctx.form.host);
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
    ...{ class: "check-row" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "checkbox",
});
(__VLS_ctx.form.imapProxyEnabled);
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "IMAP 代理地址，默认 http://127.0.0.1:7897",
});
(__VLS_ctx.form.imapProxyUrl);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "SMTP Host，默认 smtp.gmail.com",
});
(__VLS_ctx.form.smtpHost);
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
    ...{ class: "check-row" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "checkbox",
});
(__VLS_ctx.form.smtpProxyEnabled);
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "SMTP 代理地址，默认复用 IMAP 代理",
});
(__VLS_ctx.form.smtpProxyUrl);
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "hint" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "邮件文件夹，默认 INBOX",
});
(__VLS_ctx.form.folder);
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.save) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "card status" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.current.username || '未配置');
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.current.host);
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.proxyText);
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.current.smtpHost);
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.smtpProxyText);
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.current.folder);
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.current.hasAppPassword ? '已配置' : '未配置');
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.current.ready ? '是' : '否');
if (__VLS_ctx.message) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "muted" },
    });
    (__VLS_ctx.message);
}
/** @type {__VLS_StyleScopedClasses['panel']} */ ;
/** @type {__VLS_StyleScopedClasses['form']} */ ;
/** @type {__VLS_StyleScopedClasses['check-row']} */ ;
/** @type {__VLS_StyleScopedClasses['check-row']} */ ;
/** @type {__VLS_StyleScopedClasses['hint']} */ ;
/** @type {__VLS_StyleScopedClasses['card']} */ ;
/** @type {__VLS_StyleScopedClasses['status']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            form: form,
            current: current,
            message: message,
            proxyText: proxyText,
            smtpProxyText: smtpProxyText,
            save: save,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
//# sourceMappingURL=EmailConfigPage.vue.js.map