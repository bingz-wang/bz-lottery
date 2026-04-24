import WorkspaceLayout from '../views/WorkspaceLayout.vue'

export const routeCatalog = [
    {
        path: 'overview',
        name: 'overview',
        component: () => import('../views/workspace/OverviewView.vue'),
        meta: {
            menuCode: 'DASHBOARD_OVERVIEW',
            title: '指挥总览',
            navLabel: '总览',
            accent: 'sunrise',
            roles: ['LOTTERY_USER']
        }
    },
    {
        path: 'lottery',
        name: 'lottery',
        component: () => import('../views/workspace/LotteryView.vue'),
        meta: {
            menuCode: 'LOTTERY_DRAW',
            title: '抽奖工作台',
            navLabel: '抽奖台',
            accent: 'ember',
            roles: ['LOTTERY_USER']
        }
    },
    {
        path: 'prizes',
        name: 'prizes',
        component: () => import('../views/workspace/PrizeCenterView.vue'),
        meta: {
            menuCode: 'PRIZE_CENTER',
            title: '奖池中心',
            navLabel: '奖池中心',
            accent: 'ocean',
            roles: ['LOTTERY_USER']
        }
    },
    {
        path: 'operations',
        name: 'operations',
        component: () => import('../views/workspace/OperationsView.vue'),
        meta: {
            menuCode: 'OPERATIONS_CENTER',
            title: '运营权限台',
            navLabel: '运营台',
            accent: 'graphite',
            roles: ['LOTTERY_ADMIN']
        }
    },
    {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/workspace/ProfileView.vue'),
        meta: {
            menuCode: 'ACCOUNT_PROFILE',
            title: '账号与权限',
            navLabel: '我的账号',
            accent: 'iris',
            roles: ['LOTTERY_USER']
        }
    }
]

export const staticRoutes = [
    {
        path: '/',
        redirect: '/workspace'
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('../views/LoginView.vue'),
        meta: {
            public: true,
            title: '登录'
        }
    },
    {
        path: '/workspace',
        name: 'workspace',
        component: WorkspaceLayout,
        meta: {
            requiresAuth: true
        },
        children: []
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/workspace'
    }
]
