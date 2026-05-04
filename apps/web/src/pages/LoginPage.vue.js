import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
const auth = useAuthStore();
const router = useRouter();
const mode = ref('login');
const username = ref('');
const password = ref('');
const email = ref('');
const verifyCode = ref('');
const codeSending = ref(false);
const message = ref('');
async function sendCode() {
    codeSending.value = true;
    message.value = '';
    try {
        await auth.sendRegisterCode(email.value);
        message.value = '验证码已发送';
    }
    catch (e) {
        message.value = e.message;
    }
    finally {
        codeSending.value = false;
    }
}
async function submit() {
    try {
        if (mode.value === 'login')
            await auth.login(username.value, password.value);
        else
            await auth.register(username.value, password.value, email.value, verifyCode.value);
        router.push('/');
    }
    catch (e) {
        message.value = e.message;
    }
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['auth-copy']} */ ;
/** @type {__VLS_StyleScopedClasses['mini-list']} */ ;
/** @type {__VLS_StyleScopedClasses['form']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-page']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-copy']} */ ;
/** @type {__VLS_StyleScopedClasses['code-row']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "auth-page" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "auth-copy" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "eyebrow" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
(__VLS_ctx.mode === 'login' ? '欢迎回来' : '创建你的购买账户');
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "panel form" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
(__VLS_ctx.mode === 'login' ? '用户登录' : '用户注册');
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    placeholder: "用户名",
});
(__VLS_ctx.username);
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "password",
    placeholder: "密码",
});
(__VLS_ctx.password);
if (__VLS_ctx.mode === 'register') {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        placeholder: "邮箱",
    });
    (__VLS_ctx.email);
}
if (__VLS_ctx.mode === 'register') {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "code-row" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        placeholder: "邮箱验证码",
    });
    (__VLS_ctx.verifyCode);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.sendCode) },
        ...{ class: "secondary" },
        disabled: (__VLS_ctx.codeSending),
    });
    (__VLS_ctx.codeSending ? '发送中' : '发送验证码');
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.submit) },
});
(__VLS_ctx.mode === 'login' ? '登录' : '注册并登录');
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.mode = __VLS_ctx.mode === 'login' ? 'register' : 'login';
        } },
    ...{ class: "secondary" },
});
(__VLS_ctx.mode === 'login' ? '还没有账号，去注册' : '已有账号，去登录');
if (__VLS_ctx.message) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "muted" },
    });
    (__VLS_ctx.message);
}
/** @type {__VLS_StyleScopedClasses['auth-page']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-copy']} */ ;
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
/** @type {__VLS_StyleScopedClasses['panel']} */ ;
/** @type {__VLS_StyleScopedClasses['form']} */ ;
/** @type {__VLS_StyleScopedClasses['code-row']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            mode: mode,
            username: username,
            password: password,
            email: email,
            verifyCode: verifyCode,
            codeSending: codeSending,
            message: message,
            sendCode: sendCode,
            submit: submit,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
//# sourceMappingURL=LoginPage.vue.js.map