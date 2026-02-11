import axios from 'axios';
import router from "@/router";

const request = axios.create({
    baseURL: process.env.VUE_APP_BASEURL, // Backend API URL, e.g. http://localhost:9090
    timeout: 30000 // 30s request timeout
});

// Request interceptor - simplified, no auth required
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    return config;
}, error => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
});

// Response interceptor
request.interceptors.response.use(
    response => {
        let res = response.data;
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res;
        }
        return res;
    },
    error => {
        console.error('Response error:', error);
        return Promise.reject(error);
    }
);

export default request;