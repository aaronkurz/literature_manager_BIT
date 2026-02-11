import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

// Fix vue-router 3.0+ navigation duplicate error
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
    return originalPush.call(this, location).catch(err => err)
}

const routes = [
    {
        path: '/',
        redirect: '/front/home'  // Redirect to home page
    },
    {
        path: '/front',
        name: 'Front',
        component: () => import('../views/Front.vue'),
        children: [
            {
                path: 'home',
                name: 'Home',
                meta: {name: 'Home'},
                component: () => import('../views/front/Home')},
            {
                path: 'article/:id',
                name: 'ArticleDetail',
                meta: {name: 'Article Detail'},
                component: () => import('../views/front/ArticleDetail')
            },
            {
                path: 'upload',
                name: 'Upload',
                meta: {name: 'Paper Upload'},
                component: () => import('../views/front/Upload')
            },
            {
                path: 'processing/:taskId',
                name: 'ProcessingStatus',
                meta: {name: 'Processing Status'},
                component: () => import('../views/front/ProcessingStatus')
            },
            {
                path: 'graph',
                name: 'Graph',
                meta: {name: 'Knowledge Graph'},
                component: () => import('../views/front/Graph')
            },
            {
                path: 'graph-personalization',
                name: 'GraphPersonalization',
                meta: {name: 'Graph Personalization'},
                component: () => import('../views/front/GraphPersonalization')
            },
            {
                path: 'settings',
                name: 'Settings',
                meta: {name: 'Settings'},
                component: () => import('../views/front/Settings')
            },
        ]
    },
    {path: '*', name: 'NotFound', meta: {name: 'Not Found'}, component: () => import('../views/404.vue')},
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

// Remove route guard - no authentication required
router.beforeEach((to, from, next) => {
    next();
})

export default router
