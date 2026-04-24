import {createRouter, createWebHistory} from 'vue-router'
import {routeCatalog, staticRoutes} from './route-table'
import {ensureSession, hasMenu, hasRole, hasToken, markDynamicRoutes, sessionState} from '../stores/session'

const router = createRouter({
    history: createWebHistory(),
    routes: staticRoutes
})

function installDynamicRoutes() {
    if (sessionState.dynamicRoutesReady) {
        return
    }

    const names = []
    routeCatalog
        .filter((route) => hasRole(route.meta.roles ?? []) && hasMenu(route.meta.menuCode))
        .forEach((route) => {
            router.addRoute('workspace', route)
            names.push(route.name)
        })

    markDynamicRoutes(names)
}

function resolveHomeRoute() {
    const firstRoute = routeCatalog.find((route) => hasRole(route.meta.roles ?? []) && hasMenu(route.meta.menuCode))
    return firstRoute ? `/workspace/${firstRoute.path}` : '/login'
}

router.beforeEach(async (to) => {
    if (to.meta.public && !hasToken()) {
        return true
    }

    const profile = await ensureSession()
    const loggedIn = Boolean(profile && hasToken())

    if (!loggedIn && !to.meta.public) {
        return {
            path: '/login',
            query: {
                redirect: to.fullPath
            }
        }
    }

    if (loggedIn && !sessionState.dynamicRoutesReady) {
        installDynamicRoutes()
        return to.fullPath === '/workspace' ? resolveHomeRoute() : {...to, replace: true}
    }

    if (loggedIn && to.name === 'login') {
        return resolveHomeRoute()
    }

    if ((to.meta.roles && !hasRole(to.meta.roles)) || (to.meta.menuCode && !hasMenu(to.meta.menuCode))) {
        return resolveHomeRoute()
    }

    if (loggedIn && to.fullPath === '/workspace') {
        return resolveHomeRoute()
    }

    return true
})

export {routeCatalog, resolveHomeRoute}
export default router
