package com.lottery.lottery.application.strategy;

import com.lottery.common.exception.BusinessException;
import com.lottery.lottery.domain.entity.Prize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GuaranteeLadderDrawStrategy implements LotteryDrawStrategy {

    @Override
    public DrawStrategyType getType() {
        return DrawStrategyType.GUARANTEE_LADDER;
    }

    @Override
    public DrawStrategyResult selectPrize(DrawStrategyContext context) {
        DrawHistorySnapshot history = context.historySnapshot();
        GuaranteeTier targetTier = resolveTargetGuaranteeTier(history);
        if (targetTier == null) {
            Prize prize = WeightedPrizeSelector.select(context.availablePrizes());
            if (prize == null) {
                throw new BusinessException("Prize probability configuration is invalid");
            }
            return new DrawStrategyResult(prize, getType(), null, null);
        }

        GuaranteeTier actualTier = resolveActualGuaranteeTier(targetTier, context.availablePrizes());
        Prize prize = WeightedPrizeSelector.select(filterByTier(context.availablePrizes(), actualTier));
        if (prize == null) {
            throw new BusinessException("No available prizes for draw");
        }

        return new DrawStrategyResult(prize, getType(), targetTier, actualTier);
    }

    private GuaranteeTier resolveTargetGuaranteeTier(DrawHistorySnapshot history) {
        if (isThresholdReached(history.specialMissCount(), GuaranteeTier.SPECIAL)) {
            return GuaranteeTier.SPECIAL;
        }
        if (isThresholdReached(history.midTierMissCount(), GuaranteeTier.MID_TIER)) {
            return GuaranteeTier.MID_TIER;
        }
        if (isThresholdReached(history.luckyMissCount(), GuaranteeTier.LUCKY)) {
            return GuaranteeTier.LUCKY;
        }
        return null;
    }

    private boolean isThresholdReached(int missCount, GuaranteeTier tier) {
        return missCount >= tier.threshold() - 1;
    }

    private GuaranteeTier resolveActualGuaranteeTier(GuaranteeTier targetTier, List<Prize> availablePrizes) {
        return switch (targetTier) {
            case SPECIAL ->
                    findFirstAvailableTier(availablePrizes, GuaranteeTier.SPECIAL, GuaranteeTier.MID_TIER, GuaranteeTier.LUCKY, GuaranteeTier.PARTICIPATION);
            case MID_TIER ->
                    findFirstAvailableTier(availablePrizes, GuaranteeTier.MID_TIER, GuaranteeTier.LUCKY, GuaranteeTier.PARTICIPATION);
            case LUCKY -> findFirstAvailableTier(availablePrizes, GuaranteeTier.LUCKY, GuaranteeTier.PARTICIPATION);
            case PARTICIPATION -> findFirstAvailableTier(availablePrizes, GuaranteeTier.PARTICIPATION);
        };
    }

    private GuaranteeTier findFirstAvailableTier(List<Prize> availablePrizes, GuaranteeTier... tiers) {
        for (GuaranteeTier tier : tiers) {
            if (!filterByTier(availablePrizes, tier).isEmpty()) {
                return tier;
            }
        }
        throw new BusinessException("No available prizes for draw");
    }

    private List<Prize> filterByTier(List<Prize> availablePrizes, GuaranteeTier tier) {
        return availablePrizes.stream()
                .filter(prize -> tier.matches(prize.getPrizeLevelSort()))
                .toList();
    }
}
