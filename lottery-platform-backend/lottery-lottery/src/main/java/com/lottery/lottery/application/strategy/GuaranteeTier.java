package com.lottery.lottery.application.strategy;

import java.util.Set;

public enum GuaranteeTier {
    SPECIAL("special-tier", 50, Set.of(1)),
    MID_TIER("1-3 tier", 30, Set.of(2, 3, 4)),
    LUCKY("lucky-tier", 10, Set.of(5)),
    PARTICIPATION("participation-tier", 0, Set.of(6));

    private final String label;
    private final int threshold;
    private final Set<Integer> acceptedLevelSorts;

    GuaranteeTier(String label, int threshold, Set<Integer> acceptedLevelSorts) {
        this.label = label;
        this.threshold = threshold;
        this.acceptedLevelSorts = acceptedLevelSorts;
    }

    public String label() {
        return label;
    }

    public int threshold() {
        return threshold;
    }

    public boolean matches(Integer prizeLevelSort) {
        return prizeLevelSort != null && acceptedLevelSorts.contains(prizeLevelSort);
    }
}
