package com.lottery.ai.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.ai.application.dto.LotteryUserAnalysisMetrics;
import com.lottery.ai.application.dto.LotteryUserAnalysisRequest;
import com.lottery.ai.application.dto.LotteryUserAnalysisResponse;
import com.lottery.ai.application.dto.LotteryAnalysisStreamEvent;
import com.lottery.ai.domain.entity.DrawRecord;
import com.lottery.ai.infrastructure.ai.GlmChatClient;
import com.lottery.ai.infrastructure.mapper.DrawRecordMapper;
import com.lottery.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class LotteryAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LotteryAnalysisService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int HIGH_TIER_THRESHOLD = 2;

    private final DrawRecordMapper drawRecordMapper;
    private final GlmChatClient glmChatClient;

    public LotteryAnalysisService(DrawRecordMapper drawRecordMapper, GlmChatClient glmChatClient) {
        this.drawRecordMapper = drawRecordMapper;
        this.glmChatClient = glmChatClient;
    }

    public LotteryUserAnalysisResponse analyzeUserLotteryData(LotteryUserAnalysisRequest request) {
        if (request == null || request.userId() == null || request.userId() <= 0) {
            throw new BusinessException("userId must be a positive number");
        }

        List<DrawRecord> records = drawRecordMapper.selectList(new LambdaQueryWrapper<DrawRecord>()
                .eq(DrawRecord::getDeleted, false)
                .eq(DrawRecord::getUserId, request.userId())
                .orderByAsc(DrawRecord::getCreatedAt));

        if (records.isEmpty()) {
            throw new BusinessException("No draw records found for this user");
        }

        LotteryUserAnalysisMetrics metrics = buildMetrics(request.userId(), records);
        LotteryUserAnalysisResponse fallbackResponse = buildFallbackResponse(request.userId(), request.focus(), metrics);

        if (!glmChatClient.isEnabled()) {
            return fallbackResponse;
        }

        try {
            GlmChatClient.GlmNarrative narrative = glmChatClient.generateNarrative(metrics, request.focus());
            return new LotteryUserAnalysisResponse(
                    request.userId(),
                    "AI_GENERATED",
                    glmChatClient.getModelName(),
                    narrative.overview(),
                    ensureSize(narrative.insights(), buildFallbackInsights(metrics), 3),
                    ensureSize(narrative.suggestions(), buildFallbackSuggestions(metrics, request.focus()), 3),
                    metrics,
                    LocalDateTime.now()
            );
        } catch (BusinessException ex) {
            log.warn("Falling back to local lottery analysis for user {}: {}", request.userId(), ex.getMessage());
            return fallbackResponse;
        }
    }

    public SseEmitter streamUserLotteryData(LotteryUserAnalysisRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> handleStreamRequest(request, emitter));
        return emitter;
    }

    private void handleStreamRequest(LotteryUserAnalysisRequest request, SseEmitter emitter) {
        try {
            if (request == null || request.userId() == null || request.userId() <= 0) {
                throw new BusinessException("userId must be a positive number");
            }

            List<DrawRecord> records = drawRecordMapper.selectList(new LambdaQueryWrapper<DrawRecord>()
                    .eq(DrawRecord::getDeleted, false)
                    .eq(DrawRecord::getUserId, request.userId())
                    .orderByAsc(DrawRecord::getCreatedAt));

            if (records.isEmpty()) {
                throw new BusinessException("No draw records found for this user");
            }

            LotteryUserAnalysisMetrics metrics = buildMetrics(request.userId(), records);
            sendEvent(emitter, "meta", Map.of(
                    "userId", request.userId(),
                    "model", glmChatClient.getModelName(),
                    "glmEnabled", glmChatClient.isEnabled()
            ));
            sendEvent(emitter, "metrics", metrics);

            if (!glmChatClient.isEnabled()) {
                sendEvent(emitter, "complete", buildFallbackResponse(request.userId(), request.focus(), metrics));
                emitter.complete();
                return;
            }

            LotteryUserAnalysisResponse response;
            try {
                GlmChatClient.GlmNarrative narrative = glmChatClient.streamNarrative(metrics, request.focus(), chunk -> {
                    try {
                        sendEvent(emitter, chunk.type(), chunk.text());
                    } catch (Exception sendEx) {
                        throw new RuntimeException(sendEx);
                    }
                });

                response = new LotteryUserAnalysisResponse(
                        request.userId(),
                        "AI_GENERATED",
                        glmChatClient.getModelName(),
                        narrative.overview(),
                        ensureSize(narrative.insights(), buildFallbackInsights(metrics), 3),
                        ensureSize(narrative.suggestions(), buildFallbackSuggestions(metrics, request.focus()), 3),
                        metrics,
                        LocalDateTime.now()
                );
            } catch (BusinessException ex) {
                log.warn("Streaming GLM narrative fallback for user {}: {}", request.userId(), ex.getMessage());
                response = buildFallbackResponse(request.userId(), request.focus(), metrics);
            }

            sendEvent(emitter, "complete", response);
            emitter.complete();
        } catch (Exception ex) {
            log.warn("Streaming lottery analysis failed: {}", ex.getMessage(), ex);
            try {
                sendEvent(emitter, "error", ex instanceof BusinessException ? ex.getMessage() : "AI analysis stream failed");
            } catch (Exception ignored) {
            }
            emitter.complete();
        }
    }

    private LotteryUserAnalysisMetrics buildMetrics(Long userId, List<DrawRecord> records) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recentBoundary = now.minusDays(30);
        LocalDateTime previousBoundary = now.minusDays(60);

        long activeDays = records.stream()
                .map(DrawRecord::getCreatedAt)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .count();
        long recent30DayDrawCount = records.stream()
                .filter(record -> isAfter(record.getCreatedAt(), recentBoundary))
                .count();
        long previous30DayDrawCount = records.stream()
                .filter(record -> isBetween(record.getCreatedAt(), previousBoundary, recentBoundary))
                .count();
        long highTierHitCount = records.stream()
                .filter(record -> record.getPrizeLevelSort() != null && record.getPrizeLevelSort() <= HIGH_TIER_THRESHOLD)
                .count();
        long pendingReviewCount = records.stream()
                .filter(record -> record.getDrawStatus() != null && record.getDrawStatus() == 2)
                .count();

        String highestPrizeLevel = records.stream()
                .filter(record -> record.getPrizeLevelSort() != null)
                .min(Comparator.comparing(DrawRecord::getPrizeLevelSort))
                .map(record -> defaultText(record.getPrizeLevel(), "未知"))
                .orElse("未知");

        Map<String, Long> prizeCountMap = records.stream()
                .collect(Collectors.groupingBy(
                        record -> defaultText(record.getPrizeName(), "未知奖品"),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
        Map.Entry<String, Long> mostFrequentPrize = prizeCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(Map.entry("无", 0L));

        String favoriteTimeBucket = records.stream()
                .collect(Collectors.groupingBy(
                        record -> resolveTimeBucket(record.getCreatedAt()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("分布均衡");

        List<LotteryUserAnalysisMetrics.PrizeLevelMetric> prizeLevelDistribution = records.stream()
                .collect(Collectors.groupingBy(
                        record -> defaultText(record.getPrizeLevel(), "未知"),
                        Collectors.collectingAndThen(Collectors.toList(), items -> new LotteryUserAnalysisMetrics.PrizeLevelMetric(
                                items.get(0).getPrizeLevel(),
                                items.get(0).getPrizeLevelSort(),
                                items.size()
                        ))
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(metric -> metric.prizeLevelSort() == null ? Integer.MAX_VALUE : metric.prizeLevelSort()))
                .toList();

        BigDecimal probabilityAverage = records.stream()
                .map(DrawRecord::getHitProbability)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long probabilityCount = records.stream()
                .map(DrawRecord::getHitProbability)
                .filter(Objects::nonNull)
                .count();
        BigDecimal averageHitProbability = probabilityCount == 0
                ? BigDecimal.ZERO
                : probabilityAverage.divide(BigDecimal.valueOf(probabilityCount), 6, RoundingMode.HALF_UP);

        return new LotteryUserAnalysisMetrics(
                userId,
                records.size(),
                activeDays,
                recent30DayDrawCount,
                previous30DayDrawCount,
                buildTrendSummary(recent30DayDrawCount, previous30DayDrawCount),
                highestPrizeLevel,
                highTierHitCount,
                pendingReviewCount,
                favoriteTimeBucket,
                mostFrequentPrize.getKey(),
                mostFrequentPrize.getValue(),
                averageHitProbability,
                formatDateTime(records.get(0).getCreatedAt()),
                formatDateTime(records.get(records.size() - 1).getCreatedAt()),
                prizeLevelDistribution
        );
    }

    private LotteryUserAnalysisResponse buildFallbackResponse(Long userId, String focus, LotteryUserAnalysisMetrics metrics) {
        return new LotteryUserAnalysisResponse(
                userId,
                "RULE_BASED",
                glmChatClient.getModelName(),
                buildFallbackOverview(metrics, focus),
                buildFallbackInsights(metrics),
                buildFallbackSuggestions(metrics, focus),
                metrics,
                LocalDateTime.now()
        );
    }

    private String buildFallbackOverview(LotteryUserAnalysisMetrics metrics, String focus) {
        String focusText = (focus == null || focus.isBlank())
                ? "本次摘要覆盖参与活跃度、奖项层级和近期趋势。"
                : "本次摘要重点关注：" + focus.trim() + "。";
        return """
                该用户累计抽奖 %d 次，覆盖 %d 个活跃日，近 30 天参与 %d 次。当前最高命中奖项层级为 %s，高等级命中累计 %d 次，最常参与时段为 %s。%s
                """.formatted(
                metrics.totalDrawCount(),
                metrics.activeDays(),
                metrics.recent30DayDrawCount(),
                metrics.highestPrizeLevel(),
                metrics.highTierHitCount(),
                metrics.favoriteTimeBucket(),
                focusText
        ).replace('\n', ' ').trim();
    }

    private List<String> buildFallbackInsights(LotteryUserAnalysisMetrics metrics) {
        return List.of(
                "近 30 天趋势为 %s，说明近期参与度%s。".formatted(
                        metrics.trendSummary(),
                        metrics.recent30DayDrawCount() >= metrics.previous30DayDrawCount() ? "保持稳定或有所提升" : "有所回落"
                ),
                "当前最高命中奖项层级为 %s，高等级命中累计 %d 次。".formatted(
                        metrics.highestPrizeLevel(),
                        metrics.highTierHitCount()
                ),
                "命中频次最高的奖品是 %s，共出现 %d 次，抽奖时间更集中在 %s。".formatted(
                        metrics.mostFrequentPrizeName(),
                        metrics.mostFrequentPrizeCount(),
                        metrics.favoriteTimeBucket()
                )
        );
    }

    private List<String> buildFallbackSuggestions(LotteryUserAnalysisMetrics metrics, String focus) {
        String focusSuggestion = (focus == null || focus.isBlank())
                ? "如需更深入报告，建议补充活动周期对比或保底触发分析。"
                : "当前关注点是“%s”，建议继续做分窗口趋势对比。".formatted(focus.trim());
        return List.of(
                "将近 30 天参与行为拆分为周维度，判断参与变化是否由活动节点驱动。",
                metrics.pendingReviewCount() > 0
                        ? "当前有 %d 条记录待审核，建议纳入审核结果，避免高等级奖项统计滞后。".formatted(metrics.pendingReviewCount())
                        : "叠加奖品库存和活动批次，判断策略调整是否影响命中结果。",
                focusSuggestion
        );
    }

    private List<String> ensureSize(List<String> values, List<String> fallback, int size) {
        List<String> source = (values == null || values.isEmpty()) ? fallback : values;
        if (source.size() >= size) {
            return source.subList(0, size);
        }

        java.util.ArrayList<String> merged = new java.util.ArrayList<>(source);
        for (String item : fallback) {
            if (merged.size() >= size) {
                break;
            }
            if (!merged.contains(item)) {
                merged.add(item);
            }
        }
        return merged;
    }

    private boolean isAfter(LocalDateTime time, LocalDateTime boundary) {
        return time != null && !time.isBefore(boundary);
    }

    private boolean isBetween(LocalDateTime time, LocalDateTime start, LocalDateTime endExclusive) {
        return time != null && !time.isBefore(start) && time.isBefore(endExclusive);
    }

    private String resolveTimeBucket(LocalDateTime time) {
        if (time == null) {
            return "未知";
        }

        int hour = time.getHour();
        if (hour < 6) {
            return "深夜";
        }
        if (hour < 12) {
            return "上午";
        }
        if (hour < 18) {
            return "下午";
        }
        return "晚上";
    }

    private String buildTrendSummary(long recent30DayDrawCount, long previous30DayDrawCount) {
        if (recent30DayDrawCount > previous30DayDrawCount) {
            return "上升";
        }
        if (recent30DayDrawCount < previous30DayDrawCount) {
            return "下降";
        }
        return "持平";
    }

    private String formatDateTime(LocalDateTime time) {
        return time == null ? "未知" : time.format(DATE_TIME_FORMATTER);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void sendEvent(SseEmitter emitter, String type, Object data) throws java.io.IOException {
        emitter.send(SseEmitter.event()
                .name(type)
                .data(new LotteryAnalysisStreamEvent(type, data)));
    }
}
