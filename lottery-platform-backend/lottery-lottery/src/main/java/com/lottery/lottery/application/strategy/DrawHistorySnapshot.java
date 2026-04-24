package com.lottery.lottery.application.strategy;

public record DrawHistorySnapshot(
        int luckyMissCount,
        int midTierMissCount,
        int specialMissCount
) {
}
