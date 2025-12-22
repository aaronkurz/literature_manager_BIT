import axios from 'axios';
import router from "@/router";

const request = axios.create({
    baseURL: process.env.VUE_APP_BASEURL, // 后端接口地址，例如 http://localhost:9090
    timeout: 30000 // 30秒请求超时
});

// 请求拦截器 - 简化版，无需身份验证
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    return config;
}, error => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
});

// 响应拦截器
request.interceptors.response.use(
    response => {
        let res = response.data;
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res;
        }
        return res;
    },
    error => {
        console.error('响应错误:', error);
        return Promise.reject(error);
    }
);

export default request;