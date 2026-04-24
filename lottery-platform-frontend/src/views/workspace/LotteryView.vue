<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import {drawLottery, fetchLotteryRecords, fetchPrizeList, streamLotteryUserDataAnalysis} from '../../api/lottery'
import {sessionState} from '../../stores/session'

const PRIZE_COLORS = ['#ff6b57', '#ff9f43', '#ffd166', '#06d6a0', '#118ab2', '#9b5de5']
const prizes = ref([])
const records = ref([])
const highlightedPrizeId = ref(null)
const lastPrizeId = ref(null)
const isDrawing = ref(false)
const pageMessage = ref('')
const showResult = ref(false)
const latestResult = ref(null)
const assistantOpen = ref(false)
const analysisLoading = ref(false)
const analysisError = ref('')
const analysisResult = ref(null)
const analysisFocus = ref('')
const analysisMeta = ref(null)
const analysisMetrics = ref(null)
const analysisReasoning = ref('')
const analysisDraft = ref('')
let intervalId = null
let analysisRequestToken = 0

const highlightedPrize = computed(
    () => prizes.value.find((item) => item.id === highlightedPrizeId.value) ?? prizes.value[0] ?? null
)

const resultPrize = computed(() => {
    if (!latestResult.value) {
        return highlightedPrize.value
    }

    const matchedPrize = prizes.value.find((item) => item.id === latestResult.value.prizeId)
    if (matchedPrize) {
        return {
            ...matchedPrize,
            name: latestResult.value.prizeName ?? matchedPrize.name,
            level: latestResult.value.prizeLevel ?? matchedPrize.level
        }
    }

    return {
        id: latestResult.value.prizeId ?? 'latest-result',
        name: latestResult.value.prizeName ?? 'No Prize',
        level: latestResult.value.prizeLevel ?? 'Miss',
        probability: latestResult.value.hitProbability ?? '--',
        availableStock: '--',
        color: PRIZE_COLORS[0]
    }
})

const displayMetrics = computed(() => analysisResult.value?.metrics ?? analysisMetrics.value)

const analysisStageText = computed(() => {
    if (!analysisLoading.value) {
        return analysisResult.value ? 'Completed' : 'Idle'
    }
    if (analysisDraft.value.trim()) {
        return 'Generating final narrative'
    }
    if (analysisReasoning.value.trim()) {
        return 'Receiving reasoning stream'
    }
    if (analysisMetrics.value) {
        return 'Metrics ready, waiting for model output'
    }
    return 'Opening analysis stream'
})

function normalizePrize(prize, index) {
    return {
        id: prize.id,
        name: prize.prizeName,
        level: prize.prizeLevel,
        probability: prize.probability,
        availableStock: prize.availableStock,
        color: PRIZE_COLORS[index % PRIZE_COLORS.length]
    }
}

function normalizeRecord(record) {
    return {
        recordNo: record.recordNo,
        drawStatus: record.drawStatus,
        prizeId: record.prizeId,
        name: record.prizeName ?? 'No Prize',
        level: record.prizeLevel ?? 'Miss',
        remark: record.drawRemark ?? '',
        timeText: new Date(record.createdAt).toLocaleString('zh-CN', {hour12: false})
    }
}

function normalizeDrawResult(result) {
    return {
        recordNo: result.recordNo,
        prizeId: result.prizeId ?? null,
        prizeName: result.prizeName ?? 'No Prize',
        prizeLevel: result.prizeLevel ?? 'Miss',
        drawStatus: result.drawStatus ?? 0,
        hitProbability: result.hitProbability ?? '--',
        drawRemark: result.drawRemark ?? ''
    }
}

function normalizeAnalysisResult(result) {
    return {
        status: result.status,
        model: result.model,
        overview: result.overview,
        insights: result.insights ?? [],
        suggestions: result.suggestions ?? [],
        metrics: {
            totalDrawCount: result.metrics?.totalDrawCount ?? 0,
            activeDays: result.metrics?.activeDays ?? 0,
            recent30DayDrawCount: result.metrics?.recent30DayDrawCount ?? 0,
            trendSummary: result.metrics?.trendSummary ?? 'flat',
            highestPrizeLevel: result.metrics?.highestPrizeLevel ?? 'Unknown',
            highTierHitCount: result.metrics?.highTierHitCount ?? 0,
            favoriteTimeBucket: result.metrics?.favoriteTimeBucket ?? 'Unknown',
            mostFrequentPrizeName: result.metrics?.mostFrequentPrizeName ?? 'None',
            mostFrequentPrizeCount: result.metrics?.mostFrequentPrizeCount ?? 0,
            pendingReviewCount: result.metrics?.pendingReviewCount ?? 0,
            firstDrawAt: result.metrics?.firstDrawAt ?? 'Unknown',
            latestDrawAt: result.metrics?.latestDrawAt ?? 'Unknown',
            prizeLevelDistribution: (result.metrics?.prizeLevelDistribution ?? []).map((item) => ({
                prizeLevel: item.prizeLevel ?? 'Unclassified',
                prizeLevelSort: item.prizeLevelSort ?? Number.MAX_SAFE_INTEGER,
                count: item.count ?? 0
            }))
        },
        generatedAt: result.generatedAt
            ? new Date(result.generatedAt).toLocaleString('zh-CN', {hour12: false})
            : ''
    }
}

function resetAnalysisState() {
    analysisError.value = ''
    analysisResult.value = null
    analysisMeta.value = null
    analysisMetrics.value = null
    analysisReasoning.value = ''
    analysisDraft.value = ''
}

function startRolling() {
    let currentIndex = Math.max(prizes.value.findIndex((item) => item.id === highlightedPrizeId.value), 0)
    intervalId = window.setInterval(() => {
        currentIndex = (currentIndex + 1) % prizes.value.length
        highlightedPrizeId.value = prizes.value[currentIndex].id
    }, 110)
}

function stopRolling() {
    if (intervalId) {
        window.clearInterval(intervalId)
        intervalId = null
    }
}

async function loadData() {
    const [prizeData, recordData] = await Promise.all([
        fetchPrizeList(),
        fetchLotteryRecords(sessionState.profile.id, 10)
    ])
    prizes.value = prizeData.map(normalizePrize)
    records.value = recordData.map(normalizeRecord)
    highlightedPrizeId.value = prizes.value[0]?.id ?? null
    lastPrizeId.value = records.value.find((item) => item.prizeId)?.prizeId ?? null
}

async function handleDraw() {
    if (isDrawing.value || !prizes.value.length) {
        return
    }
    isDrawing.value = true
    showResult.value = false
    pageMessage.value = ''
    startRolling()

    try {
        const result = await drawLottery(sessionState.profile.id, `REQ-${sessionState.profile.id}-${Date.now()}`)
        const normalizedResult = normalizeDrawResult(result)
        latestResult.value = normalizedResult
        const matchedPrize = prizes.value.find((item) => item.id === normalizedResult.prizeId)
        window.setTimeout(async () => {
            stopRolling()
            if (matchedPrize) {
                highlightedPrizeId.value = matchedPrize.id
                lastPrizeId.value = matchedPrize.id
            } else {
                highlightedPrizeId.value = prizes.value[0]?.id ?? null
                lastPrizeId.value = null
            }
            pageMessage.value = ''
            await loadData()
            showResult.value = true
            isDrawing.value = false
        }, 1600)
    } catch (error) {
        stopRolling()
        isDrawing.value = false
        pageMessage.value = error.message || 'Draw failed. Please try again later.'
    }
}

async function handleAnalyze() {
    if (analysisLoading.value || !sessionState.profile?.id) {
        return
    }

    const requestToken = Date.now()
    analysisRequestToken = requestToken
    analysisLoading.value = true
    assistantOpen.value = true
    resetAnalysisState()

    try {
        await streamLotteryUserDataAnalysis(sessionState.profile.id, analysisFocus.value.trim(), {
            onMeta(payload) {
                if (analysisRequestToken !== requestToken) {
                    return
                }
                analysisMeta.value = payload
            },
            onMetrics(payload) {
                if (analysisRequestToken !== requestToken) {
                    return
                }
                analysisMetrics.value = normalizeAnalysisResult({metrics: payload}).metrics
            },
            onReasoning(text) {
                if (analysisRequestToken !== requestToken || !text) {
                    return
                }
                analysisReasoning.value += text
            },
            onDelta(text) {
                if (analysisRequestToken !== requestToken || !text) {
                    return
                }
                analysisDraft.value += text
            },
            onComplete(payload) {
                if (analysisRequestToken !== requestToken) {
                    return
                }
                analysisResult.value = normalizeAnalysisResult(payload)
            },
            onError(message) {
                if (analysisRequestToken !== requestToken) {
                    return
                }
                analysisError.value = message || 'AI analysis is temporarily unavailable.'
            }
        })

        if (analysisRequestToken === requestToken && !analysisResult.value && !analysisError.value) {
            analysisError.value = 'The analysis stream finished without a final result.'
        }
    } catch (error) {
        if (analysisRequestToken === requestToken) {
            analysisError.value = error.message || 'AI analysis is temporarily unavailable.'
        }
    } finally {
        if (analysisRequestToken === requestToken) {
            analysisLoading.value = false
        }
    }
}

onMounted(loadData)
onBeforeUnmount(() => {
    stopRolling()
    analysisRequestToken += 1
})
</script>

<template>
  <div class="workspace-grid two-columns">
    <section class="panel wide-panel">
      <div class="section-header section-header-stack">
        <div>
          <h3>Lottery Console</h3>
          <p class="section-subtitle">Start a draw to play the rolling animation first, then reveal the final result in the modal.</p>
        </div>
        <div class="action-row">
          <button :disabled="isDrawing" class="primary-btn action-btn" @click="handleDraw">
            {{ isDrawing ? 'Drawing...' : 'Start Draw' }}
          </button>
        </div>
      </div>
      <p v-if="pageMessage" class="page-message">{{ pageMessage }}</p>
      <div class="prize-grid workspace-prize-grid">
        <article
            v-for="prize in prizes"
            :key="prize.id"
            :class="{ active: prize.id === highlightedPrizeId, winner: prize.id === lastPrizeId && !isDrawing }"
            :style="{ '--accent': prize.color }"
            class="prize-card"
        >
          <span class="prize-level">{{ prize.level }}</span>
          <strong>{{ prize.name }}</strong>
          <p class="prize-meta">Probability {{ prize.probability }} / Stock {{ prize.availableStock }}</p>
        </article>
      </div>
    </section>

    <section class="panel">
      <div class="section-header">
        <h3>Recent Records</h3>
        <span>{{ records.length }} items</span>
      </div>
      <div class="record-list compact-record-list">
        <article v-for="record in records" :key="record.recordNo" class="record-item">
          <div>
            <strong>{{ record.level }}</strong>
            <p>{{ record.name }}</p>
          </div>
          <div class="record-side">
            <span>{{ record.timeText }}</span>
            <small>{{ record.remark }}</small>
          </div>
        </article>
      </div>
    </section>

    <transition name="result-pop">
      <div v-if="showResult && resultPrize" class="result-mask" @click.self="showResult = false">
        <div :style="{ '--accent': resultPrize.color }" class="result-dialog">
          <p>{{ latestResult?.drawStatus === 2 ? 'Draw submitted, waiting for review' : 'You hit a prize' }}</p>
          <h3>{{ resultPrize.level }}</h3>
          <strong>{{ resultPrize.name }}</strong>
          <br/>
          <small class="result-remark">{{ latestResult?.drawRemark }}</small>
          <div class="result-actions">
            <button class="primary-btn result-btn" @click="showResult = false">Close Result</button>
          </div>
        </div>
      </div>
    </transition>

    <button
        :class="{ open: assistantOpen }"
        class="ai-fab"
        type="button"
        @click="assistantOpen = !assistantOpen"
    >
      <span>AI</span>
      <small>Helper</small>
    </button>

    <transition name="assistant-pop">
      <aside v-if="assistantOpen" class="ai-panel">
        <div class="ai-panel-head">
          <div>
            <p class="eyebrow">AI Assistant</p>
            <h3>Lottery Analysis</h3>
          </div>
          <button class="ai-panel-close" type="button" @click="assistantOpen = false">Close</button>
        </div>

        <p class="ai-panel-copy">
          The assistant first receives aggregated lottery metrics, then the model produces the final analysis and suggestions.
        </p>

        <section class="ai-capability">
          <div>
            <strong>Current Scope</strong>
            <p>Activity trend, prize level performance, time-bucket preference, and pending-review status.</p>
          </div>
          <button :disabled="analysisLoading" class="primary-btn ai-action-btn" @click="handleAnalyze">
            {{ analysisLoading ? 'Analyzing...' : 'Analyze My Data' }}
          </button>
        </section>

        <label class="field-label" for="analysis-focus">Optional focus</label>
        <input
            id="analysis-focus"
            v-model="analysisFocus"
            class="user-input"
            maxlength="50"
            placeholder="Example: are high-tier prizes improving recently?"
            type="text"
        />

        <p class="ai-stream-status">
          <span class="signed-badge">{{ analysisMeta?.model || 'lottery-ai' }}</span>
          <small>{{ analysisStageText }}</small>
        </p>

        <p v-if="analysisError" class="page-message">{{ analysisError }}</p>

        <section v-if="displayMetrics || analysisLoading || analysisResult" class="ai-report">
          <div v-if="analysisResult" class="ai-report-meta">
            <span class="signed-badge">{{
                analysisResult.status === 'AI_GENERATED' ? analysisResult.model : 'rule-based'
              }}</span>
            <small v-if="analysisResult.generatedAt">{{ analysisResult.generatedAt }}</small>
          </div>

          <div v-if="displayMetrics" class="ai-metrics-grid">
            <article class="ai-metric-card">
              <span>Total draws</span>
              <strong>{{ displayMetrics.totalDrawCount }}</strong>
            </article>
            <article class="ai-metric-card">
              <span>Active days</span>
              <strong>{{ displayMetrics.activeDays }}</strong>
            </article>
            <article class="ai-metric-card">
              <span>Last 30 days</span>
              <strong>{{ displayMetrics.recent30DayDrawCount }}</strong>
            </article>
            <article class="ai-metric-card">
              <span>Highest level</span>
              <strong>{{ displayMetrics.highestPrizeLevel }}</strong>
            </article>
          </div>

          <div v-if="displayMetrics" class="ai-tag-row">
            <span class="ai-tag">Trend: {{ displayMetrics.trendSummary }}</span>
            <span class="ai-tag">High tier: {{ displayMetrics.highTierHitCount }}</span>
            <span class="ai-tag">Time bucket: {{ displayMetrics.favoriteTimeBucket }}</span>
            <span class="ai-tag">Pending review: {{ displayMetrics.pendingReviewCount }}</span>
          </div>

          <div v-if="analysisReasoning" class="ai-section">
            <h4>Reasoning Stream</h4>
            <pre class="ai-stream-block">{{ analysisReasoning }}</pre>
          </div>

          <div v-if="analysisDraft && !analysisResult" class="ai-section">
            <h4>Draft Output</h4>
            <pre class="ai-stream-block">{{ analysisDraft }}</pre>
          </div>

          <template v-if="analysisResult">
            <p class="ai-overview">{{ analysisResult.overview }}</p>

            <div class="ai-section">
              <h4>Insights</h4>
              <ul class="ai-list">
                <li v-for="item in analysisResult.insights" :key="item">{{ item }}</li>
              </ul>
            </div>

            <div class="ai-section">
              <h4>Suggestions</h4>
              <ul class="ai-list">
                <li v-for="item in analysisResult.suggestions" :key="item">{{ item }}</li>
              </ul>
            </div>

            <div class="ai-section">
              <h4>Prize Level Distribution</h4>
              <div class="ai-level-list">
                <article
                    v-for="item in analysisResult.metrics.prizeLevelDistribution"
                    :key="`${item.prizeLevel}-${item.prizeLevelSort}`"
                    class="ai-level-item"
                >
                  <span>{{ item.prizeLevel }}</span>
                  <strong>{{ item.count }} hits</strong>
                </article>
              </div>
            </div>
          </template>
        </section>
      </aside>
    </transition>
  </div>
</template>
