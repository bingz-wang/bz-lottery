package com.lottery.lottery.application.strategy;

import com.lottery.lottery.domain.entity.DrawRecord;
import com.lottery.lottery.domain.entity.Prize;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GuaranteeLadderDrawStrategyTest {

    private final GuaranteeLadderDrawStrategy strategy = new GuaranteeLadderDrawStrategy();
    private final ProbabilityOnlyDrawStrategy probabilityOnlyDrawStrategy = new ProbabilityOnlyDrawStrategy();

    @Test
    void shouldGuaranteeLuckyPrizeOnTenthMiss() {
        Prize luckyPrize = prize(5L, 5, "Lucky Prize");
        Prize participationPrize = prize(6L, 6, "Participation Prize");

        DrawStrategyResult result = strategy.selectPrize(context(
                List.of(luckyPrize, participationPrize),
                history(9, 0, 0)
        ));

        assertEquals(DrawStrategyType.GUARANTEE_LADDER, result.strategyType());
        assertEquals(GuaranteeTier.LUCKY, result.targetGuaranteeTier());
        assertEquals(GuaranteeTier.LUCKY, result.actualGuaranteeTier());
        assertEquals(5, result.prize().getPrizeLevelSort());
    }

    @Test
    void shouldPrioritizeMidTierOverLuckyWhenBothThresholdsReached() {
        Prize midTierPrize = prize(2L, 2, "First Prize");
        Prize luckyPrize = prize(5L, 5, "Lucky Prize");

        DrawStrategyResult result = strategy.selectPrize(context(
                List.of(midTierPrize, luckyPrize),
                history(29, 29, 0)
        ));

        assertEquals(GuaranteeTier.MID_TIER, result.targetGuaranteeTier());
        assertEquals(GuaranteeTier.MID_TIER, result.actualGuaranteeTier());
        assertEquals(2, result.prize().getPrizeLevelSort());
    }

    @Test
    void higherPrizeHistoryShouldNotResetLuckyGuaranteeCounter() {
        Prize luckyPrize = prize(5L, 5, "Lucky Prize");
        Prize specialPrize = prize(1L, 1, "Special Prize");

        DrawStrategyResult result = strategy.selectPrize(new DrawStrategyContext(
                10001L,
                List.of(luckyPrize, specialPrize),
                List.of(record(1), record(6), record(6), record(6), record(6), record(6), record(6), record(6), record(6)),
                new DrawHistorySnapshot(9, 8, 0)
        ));

        assertEquals(GuaranteeTier.LUCKY, result.targetGuaranteeTier());
        assertEquals(GuaranteeTier.LUCKY, result.actualGuaranteeTier());
    }

    @Test
    void shouldDowngradeSpecialGuaranteeToMidTierWhenSpecialIsUnavailable() {
        Prize midTierPrize = prize(3L, 3, "Second Prize");
        Prize luckyPrize = prize(5L, 5, "Lucky Prize");

        DrawStrategyResult result = strategy.selectPrize(context(
                List.of(midTierPrize, luckyPrize),
                history(9, 29, 49)
        ));

        assertEquals(GuaranteeTier.SPECIAL, result.targetGuaranteeTier());
        assertEquals(GuaranteeTier.MID_TIER, result.actualGuaranteeTier());
        assertEquals(3, result.prize().getPrizeLevelSort());
    }

    @Test
    void probabilityOnlyStrategyShouldLeaveGuaranteeMetadataEmpty() {
        Prize participationPrize = prize(6L, 6, "Participation Prize");

        DrawStrategyResult result = probabilityOnlyDrawStrategy.selectPrize(new DrawStrategyContext(
                10001L,
                List.of(participationPrize),
                List.of(),
                new DrawHistorySnapshot(0, 0, 0)
        ));

        assertEquals(DrawStrategyType.PROBABILITY_ONLY, result.strategyType());
        assertNull(result.targetGuaranteeTier());
        assertNull(result.actualGuaranteeTier());
        assertEquals(6, result.prize().getPrizeLevelSort());
    }

    private DrawStrategyContext context(List<Prize> prizes, DrawHistorySnapshot historySnapshot) {
        return new DrawStrategyContext(10001L, prizes, List.of(), historySnapshot);
    }

    private Prize prize(Long id, int levelSort, String level) {
        Prize prize = new Prize();
        prize.setId(id);
        prize.setPrizeCode("CODE-" + id);
        prize.setPrizeName(level);
        prize.setPrizeLevel(level);
        prize.setPrizeLevelSort(levelSort);
        prize.setProbability(BigDecimal.ONE);
        prize.setAvailableStock(10);
        prize.setStatus(1);
        return prize;
    }

    private DrawRecord record(int levelSort) {
        DrawRecord record = new DrawRecord();
        record.setPrizeLevelSort(levelSort);
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }

    private DrawHistorySnapshot history(int luckyMissCount, int midTierMissCount, int specialMissCount) {
        return new DrawHistorySnapshot(luckyMissCount, midTierMissCount, specialMissCount);
    }
}
