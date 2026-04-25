export const TOKEN_NAME = 'satoken'
const PROFILE_KEY = 'lottery-user-profile'

export function getToken() {
    return window.localStorage.getItem(TOKEN_NAME)
}

export function setSession(session) {
    if (session?.tokenValue) {
        window.localStorage.setItem(TOKEN_NAME, session.tokenValue)
    }
    if (session?.profile) {
        setStoredProfile(session.profile)
    }
}

export function setStoredProfile(profile) {
    if (profile) {
        window.localStorage.setItem(PROFILE_KEY, JSON.stringify(profile))
    }
}

export function clearSession() {
    window.localStorage.removeItem(TOKEN_NAME)
    window.localStorage.removeItem(PROFILE_KEY)
}

export function getStoredProfile() {
    const raw = window.localStorage.getItem(PROFILE_KEY)
    if (!raw) {
        return null
    }

    try {
        return JSON.parse(raw)
    } catch {
        clearSession()
        return null
    }
}

function buildHeaders(options = {}) {
    const token = getToken()
    return {
        'Content-Type': 'application/json',
        ...(token ? {[TOKEN_NAME]: token} : {}),
        ...(options.headers ?? {})
    }
}

export async function requestJson(url, options = {}) {
    const response = await fetch(url, {
        headers: buildHeaders(options),
        ...options
    })

    const payload = await response.json().catch(() => null)

    if (response.status === 401) {
        clearSession()
    }

    if (!response.ok || !payload || payload.code !== '00000') {
        throw new Error(payload?.message ?? `请求失败：${response.status}`)
    }

    return payload.data
}

export async function requestEventStream(url, options = {}, handlers = {}) {
    const response = await fetch(url, {
        headers: {
            Accept: 'text/event-stream',
            ...buildHeaders(options)
        },
        ...options
    })

    if (response.status === 401) {
        clearSession()
    }

    if (!response.ok) {
        const message = await response.text().catch(() => '')
        throw new Error(message || `请求失败：${response.status}`)
    }

    if (!response.body) {
        throw new Error('当前浏览器不支持流式响应')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    try {
        while (true) {
            const {done, value} = await reader.read()
            if (done) {
                break
            }

            buffer += decoder.decode(value, {stream: true})
            const chunks = buffer.split(/\r?\n\r?\n/)
            buffer = chunks.pop() ?? ''

            for (const chunk of chunks) {
                handleSseChunk(chunk, handlers)
            }
        }

        const remaining = buffer + decoder.decode()
        if (remaining.trim()) {
            handleSseChunk(remaining, handlers)
        }
    } finally {
        reader.releaseLock()
    }
}

function handleSseChunk(chunk, handlers) {
    const lines = chunk.split(/\r?\n/)
    let eventName = 'message'
    const dataLines = []

    for (const line of lines) {
        if (!line || line.startsWith(':')) {
            continue
        }

        if (line.startsWith('event:')) {
            eventName = line.slice(6).trim()
            continue
        }

        if (line.startsWith('data:')) {
            dataLines.push(line.slice(5).trim())
        }
    }

    if (!dataLines.length) {
        return
    }

    const rawData = dataLines.join('\n')
    let payload = rawData

    try {
        payload = JSON.parse(rawData)
    } catch {
        payload = rawData
    }

    handlers.onEvent?.({
        event: eventName,
        data: payload
    })

    if (eventName === 'error') {
        const message = payload?.data ?? payload?.message ?? '流式分析失败'
        handlers.onError?.(message, payload)
        return
    }

    handlers[eventName]?.(payload)
}
