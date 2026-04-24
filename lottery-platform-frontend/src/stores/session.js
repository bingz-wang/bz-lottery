import {reactive} from 'vue'
import {fetchCurrentUser, logoutUser} from '../api/auth'
import {clearSession, getStoredProfile, getToken, setStoredProfile} from '../api/http'

const storedProfile = getStoredProfile()

export const sessionState = reactive({
    profile: storedProfile,
    roles: storedProfile?.roles ?? [],
    initialized: false,
    dynamicRoutesReady: false,
    dynamicRouteNames: []
})

export function hasToken() {
    return Boolean(getToken())
}

export function hasRole(requiredRoles = []) {
    if (!requiredRoles.length) {
        return true
    }
    return requiredRoles.some((role) => sessionState.roles.includes(role))
}

export function hasMenu(menuCode) {
    if (!menuCode) {
        return true
    }
    return (sessionState.profile?.menus ?? []).some((menu) => menu.menuCode === menuCode)
}

export function setProfile(profile) {
    sessionState.profile = profile
    sessionState.roles = profile?.roles ?? []
    if (profile) {
        setStoredProfile(profile)
    }
}

export function resetSessionState() {
    clearSession()
    sessionState.profile = null
    sessionState.roles = []
    sessionState.initialized = true
    sessionState.dynamicRoutesReady = false
    sessionState.dynamicRouteNames = []
}

export function markDynamicRoutes(names) {
    sessionState.dynamicRoutesReady = true
    sessionState.dynamicRouteNames = names
}

export async function ensureSession() {
    if (!hasToken()) {
        sessionState.initialized = true
        return null
    }
    if (sessionState.initialized && sessionState.profile) {
        return sessionState.profile
    }

    try {
        const profile = await fetchCurrentUser()
        setProfile(profile)
        sessionState.initialized = true
        return profile
    } catch {
        resetSessionState()
        return null
    }
}

export async function signOut() {
    try {
        await logoutUser()
    } finally {
        resetSessionState()
    }
}
