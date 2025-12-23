import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

// 解决导航栏或者底部导航tabBar中的vue-router在3.0版本以上频繁点击菜单报错的问题。
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
    return originalPush.call(this, location).catch(err => err)
}

const routes = [
    {
        path: '/',
        redirect: '/front/home'  // 直接重定向到主页
    },
    {
        path: '/front',
        name: 'Front',
        component: () => import('../views/Front.vue'),
        children: [
            {
                path: 'home',
                name: 'Home',
                meta: {name: '系统首页'},
                component: () => import('../views/front/Home')},
            {
                path: 'article/:id',
                name: 'ArticleDetail',
                meta: {name: '文章详情'},
                component: () => import('../views/front/ArticleDetail')
            },
            {
                path: 'upload',
                name: 'Upload',
                meta: {name: '论文上传'},
                component: () => import('../views/front/Upload')
            },
            {
                path: 'processing/:taskId',
                name: 'ProcessingStatus',
                meta: {name: '处理状态'},
                component: () => import('../views/front/ProcessingStatus')
            },
            {
                path: 'graph',
                name: 'Graph',
                meta: {name: '知识图谱'},
                component: () => import('../views/front/Graph')
            },
            {
                path: 'graph-personalization',
                name: 'GraphPersonalization',
                meta: {name: '图谱个性化'},
                component: () => import('../views/front/GraphPersonalization')
            },
            {
                path: 'settings',
                name: 'Settings',
                meta: {name: '设置'},
                component: () => import('../views/front/Settings')
            },
        ]
    },
    {path: '*', name: 'NotFound', meta: {name: '无法访问'}, component: () => import('../views/404.vue')},
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

// 移除路由守卫 - 无需身份验证
router.beforeEach((to, from, next) => {
    next();
})

export default router
