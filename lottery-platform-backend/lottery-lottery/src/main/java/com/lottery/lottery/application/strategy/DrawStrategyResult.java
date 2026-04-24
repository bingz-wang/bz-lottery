package com.lottery.lottery.application.strategy;

import com.lottery.lottery.domain.entity.Prize;

public record DrawStrategyResult(
        Prize prize,
        DrawStrategyType strategyType,
        GuaranteeTier targetGuaranteeTier,
        GuaranteeTier actualGuaranteeTier
) {
    public boolean isGuaranteeHit() {
        return targetGuaranteeTier != null;
    }

    public boolean isDowngradedGuarantee() {
        return isGuaranteeHit() && actualGuaranteeTier != null && actualGuaranteeTier != targetGuaranteeTier;
    }
}
