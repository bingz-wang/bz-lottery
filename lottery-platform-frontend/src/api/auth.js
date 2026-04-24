import {clearSession, requestJson, setSession} from './http'

export async function registerUser(payload) {
    const data = await requestJson('/lottery-user/api/user/auth/register', {
        method: 'POST',
        body: JSON.stringify(payload)
    })
    setSession(data)
    return data
}

export async function loginUser(payload) {
    const data = await requestJson('/lottery-user/api/user/auth/login', {
        method: 'POST',
        body: JSON.stringify(payload)
    })
    setSession(data)
    return data
}

export function fetchCurrentUser() {
    return requestJson('/lottery-user/api/user/auth/me')
}

export async function logoutUser() {
    try {
        await requestJson('/lottery-user/api/user/auth/logout', {
            method: 'POST'
        })
    } finally {
        clearSession()
    }
}
