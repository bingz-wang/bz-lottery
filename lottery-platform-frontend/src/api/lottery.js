import {requestEventStream, requestJson} from './http'

export function fetchPrizeList() {
    return requestJson('/lottery-award/api/award/prizes')
}

export function fetchLotteryRecords(userId, limit = 10) {
    return requestJson(`/lottery-lottery/api/lottery/records?userId=${userId}&limit=${limit}`)
}

export function drawLottery(userId, requestNo) {
    return requestJson('/lottery-lottery/api/lottery/draw', {
        method: 'POST',
        body: JSON.stringify({
            userId,
            requestNo
        })
    })
}

export function analyzeLotteryUserData(userId, focus = '') {
    return requestJson('/lottery-ai/api/ai/lottery-analysis/user', {
        method: 'POST',
        body: JSON.stringify({
            userId,
            focus
        })
    })
}

export function streamLotteryUserDataAnalysis(userId, focus = '', handlers = {}) {
    return requestEventStream('/lottery-ai/api/ai/lottery-analysis/user/stream', {
        method: 'POST',
        body: JSON.stringify({
            userId,
            focus
        })
    }, {
        meta(payload) {
            handlers.onMeta?.(unwrapStreamPayload(payload))
        },
        metrics(payload) {
            handlers.onMetrics?.(unwrapStreamPayload(payload))
        },
        reasoning(payload) {
            handlers.onReasoning?.(unwrapStreamText(payload))
        },
        delta(payload) {
            handlers.onDelta?.(unwrapStreamText(payload))
        },
        complete(payload) {
            handlers.onComplete?.(unwrapStreamPayload(payload))
        },
        onError(message, payload) {
            handlers.onError?.(message, payload)
        }
    })
}

function unwrapStreamPayload(payload) {
    if (payload && typeof payload === 'object' && 'data' in payload) {
        return payload.data
    }
    return payload
}

function unwrapStreamText(payload) {
    const value = unwrapStreamPayload(payload)
    return typeof value === 'string' ? value : ''
}
