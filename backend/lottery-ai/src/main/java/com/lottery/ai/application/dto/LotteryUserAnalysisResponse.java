package com.lottery.ai.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LotteryUserAnalysisResponse(
        Long userId,
        String status,
        String model,
        String overview,
        List<String> insights,
        List<String> suggestions,
        LotteryUserAnalysisMetrics metrics,
        LocalDateTime generatedAt
) {
}
