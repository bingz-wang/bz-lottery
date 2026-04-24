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
                .map(record -> defaultText(record.getPrizeLevel(), "Unknown"))
                .orElse("Unknown");

        Map<String, Long> prizeCountMap = records.stream()
                .collect(Collectors.groupingBy(
                        record -> defaultText(record.getPrizeName(), "Unknown prize"),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
        Map.Entry<String, Long> mostFrequentPrize = prizeCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(Map.entry("None", 0L));

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
                .orElse("Balanced");

        List<LotteryUserAnalysisMetrics.PrizeLevelMetric> prizeLevelDistribution = records.stream()
                .collect(Collectors.groupingBy(
                        record -> defaultText(record.getPrizeLevel(), "Unknown"),
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
                ? "The summary covers activity, prize level, and recent trend."
                : "This summary focuses on: " + focus.trim() + ".";
        return """
                The user has %d total draws across %d active days, including %d draws in the last 30 days. The highest prize level reached is %s, high-tier hits total %d, and the most frequent draw period is %s. %s
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
                "The 30-day trend is %s, which suggests the recent participation level is %s.".formatted(
                        metrics.trendSummary(),
                        metrics.recent30DayDrawCount() >= metrics.previous30DayDrawCount() ? "stable or increasing" : "cooling down"
                ),
                "The highest achieved prize level is %s, and high-tier hits total %d.".formatted(
                        metrics.highestPrizeLevel(),
                        metrics.highTierHitCount()
                ),
                "The most frequent prize is %s with %d occurrences, and draws are more concentrated in %s.".formatted(
                        metrics.mostFrequentPrizeName(),
                        metrics.mostFrequentPrizeCount(),
                        metrics.favoriteTimeBucket()
                )
        );
    }

    private List<String> buildFallbackSuggestions(LotteryUserAnalysisMetrics metrics, String focus) {
        String focusSuggestion = (focus == null || focus.isBlank())
                ? "If you need a deeper report, add campaign-period comparison or guarantee-trigger analysis next."
                : "The current focus is \"%s\". A window-based trend comparison would be a good next step.".formatted(focus.trim());
        return List.of(
                "Break the recent 30-day activity into weekly slices to see whether participation is event-driven.",
                metrics.pendingReviewCount() > 0
                        ? "There are %d records pending review. Include review outcomes in analysis to avoid lagged high-tier statistics.".formatted(metrics.pendingReviewCount())
                        : "Overlay prize stock and campaign batches to understand whether strategy changes affected results.",
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
            return "Unknown";
        }

        int hour = time.getHour();
        if (hour < 6) {
            return "Late night";
        }
        if (hour < 12) {
            return "Morning";
        }
        if (hour < 18) {
            return "Afternoon";
        }
        return "Evening";
    }

    private String buildTrendSummary(long recent30DayDrawCount, long previous30DayDrawCount) {
        if (recent30DayDrawCount > previous30DayDrawCount) {
            return "up";
        }
        if (recent30DayDrawCount < previous30DayDrawCount) {
            return "down";
        }
        return "flat";
    }

    private String formatDateTime(LocalDateTime time) {
        return time == null ? "Unknown" : time.format(DATE_TIME_FORMATTER);
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
