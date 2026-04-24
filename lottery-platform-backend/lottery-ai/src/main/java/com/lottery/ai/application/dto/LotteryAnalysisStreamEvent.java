package com.lottery.ai.application.dto;

public class LotteryAnalysisStreamEvent {

    private final String type;
    private final Object data;

    public LotteryAnalysisStreamEvent(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
