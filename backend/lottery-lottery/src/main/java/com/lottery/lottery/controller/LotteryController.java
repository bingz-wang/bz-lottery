package com.lottery.lottery.controller;

import com.lottery.common.response.ApiResponse;
import com.lottery.lottery.application.dto.DrawRecordView;
import com.lottery.lottery.application.dto.DrawRequest;
import com.lottery.lottery.application.dto.DrawResponse;
import com.lottery.lottery.application.service.LotteryDrawService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lottery")
public class LotteryController {

    private final LotteryDrawService lotteryDrawService;

    public LotteryController(LotteryDrawService lotteryDrawService) {
        this.lotteryDrawService = lotteryDrawService;
    }

    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.success(Map.of(
                "service", "lottery-lottery",
                "status", "UP"
        ));
    }

    @PostMapping("/draw")
    public ApiResponse<DrawResponse> draw(@RequestBody DrawRequest request) {
        return ApiResponse.success(lotteryDrawService.draw(request));
    }

    @GetMapping("/records")
    public ApiResponse<List<DrawRecordView>> listRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        return ApiResponse.success(lotteryDrawService.listRecords(userId, limit));
    }

}
