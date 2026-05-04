import axios from 'axios';
export const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    timeout: 35000
});
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('web_token');
    if (token)
        config.headers.Authorization = `Bearer ${token}`;
    return config;
});
api.interceptors.response.use((response) => {
    const body = response.data;
    if (body.code !== 0)
        throw new Error(body.message || '请求失败');
    return body;
}, (error) => {
    const message = error.code === 'ECONNABORTED'
        ? '查询超时，请稍后重试或检查后台邮箱配置'
        : error.response?.data?.message || error.message || '网络请求失败';
    throw new Error(message);
});
//# sourceMappingURL=client.js.map