package com.lottery.ai.controller;

import com.lottery.ai.application.dto.LotteryUserAnalysisRequest;
import com.lottery.ai.application.dto.LotteryUserAnalysisResponse;
import com.lottery.ai.application.service.LotteryAnalysisService;
import com.lottery.common.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiAnalysisController {

    private final LotteryAnalysisService lotteryAnalysisService;

    public AiAnalysisController(LotteryAnalysisService lotteryAnalysisService) {
        this.lotteryAnalysisService = lotteryAnalysisService;
    }

    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.success(Map.of(
                "service", "lottery-ai",
                "status", "UP"
        ));
    }

    @PostMapping("/lottery-analysis/user")
    public ApiResponse<LotteryUserAnalysisResponse> analyzeUserLotteryData(
            @RequestBody LotteryUserAnalysisRequest request
    ) {
        return ApiResponse.success(lotteryAnalysisService.analyzeUserLotteryData(request));
    }

    @PostMapping("/lottery-analysis/user/raw")
    public LotteryUserAnalysisResponse analyzeUserLotteryDataRaw(
            @RequestBody LotteryUserAnalysisRequest request
    ) {
        return lotteryAnalysisService.analyzeUserLotteryData(request);
    }

    @PostMapping(value = "/lottery-analysis/user/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUserLotteryData(@RequestBody LotteryUserAnalysisRequest request) {
        return lotteryAnalysisService.streamUserLotteryData(request);
    }
}
