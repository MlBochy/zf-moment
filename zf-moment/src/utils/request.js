import axios from 'axios';
import { ElMessage } from 'element-plus'
import router from '@/router'

const baseURL = 'http://localhost:8080';  // 修改为实际的后端地址
const instance = axios.create({ 
    baseURL,
    timeout: 5000,  // 添加超时设置
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
    withCredentials: true  // 允许跨域请求携带凭证
})

// 添加请求拦截器
instance.interceptors.request.use(
    (config) => {
        console.log('Request config:', config);  // 添加请求日志
        // 注册和登录接口不需要添加token
        if (!config.url.includes('/user/register') && !config.url.includes('/user/login')) {
            const token = localStorage.getItem('token');
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
        }
        
        // 对于注册和登录接口，使用JSON格式
        if (config.url.includes('/user/register') || config.url.includes('/user/login')) {
            config.headers['Content-Type'] = 'application/json'
        }
        
        return config;
    },
    (err) => {
        console.error('Request error:', err);  // 添加错误日志
        return Promise.reject(err)
    }
)

// 添加响应拦截器
instance.interceptors.response.use(
    result => {
        console.log('Response data:', result.data);  // 添加响应日志
        if (result.data.code === 1) {  // 修改为判断code === 1
            return result.data;
        }
        ElMessage.error(result.data.msg || '服务异常')
        return Promise.reject(result.data)
    },
    err => {
        console.error('Error response:', err.response);  // 添加错误日志
        
        if (err.response) {
            switch (err.response.status) {
                case 401:
                    ElMessage.error('未登录或登录已过期')
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('没有权限')
                    break
                case 404:
                    ElMessage.error('请求的资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error(err.response.data?.msg || '请求失败')
            }
        } else {
            ElMessage.error('网络错误，请检查您的网络连接')
        }
        
        return Promise.reject(err)
    }
)

export default instance