package com.lottery.lottery.application.strategy;

public enum DrawStrategyType {
    PROBABILITY_ONLY,
    GUARANTEE_LADDER;

    public static DrawStrategyType fromConfigValue(String value) {
        if (value == null || value.isBlank()) {
            return PROBABILITY_ONLY;
        }

        for (DrawStrategyType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        return PROBABILITY_ONLY;
    }
}
