package com.lottery.ai.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record LotteryUserAnalysisMetrics(
        Long userId,
        long totalDrawCount,
        long activeDays,
        long recent30DayDrawCount,
        long previous30DayDrawCount,
        String trendSummary,
        String highestPrizeLevel,
        long highTierHitCount,
        long pendingReviewCount,
        String favoriteTimeBucket,
        String mostFrequentPrizeName,
        long mostFrequentPrizeCount,
        BigDecimal averageHitProbability,
        String firstDrawAt,
        String latestDrawAt,
        List<PrizeLevelMetric> prizeLevelDistribution
) {

    public record PrizeLevelMetric(
            String prizeLevel,
            Integer prizeLevelSort,
            long count
    ) {
    }
}
