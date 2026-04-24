package com.lottery.award.controller;

import com.lottery.award.application.dto.PrizeView;
import com.lottery.award.application.service.PrizeQueryService;
import com.lottery.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/award")
public class AwardController {

    private final PrizeQueryService prizeQueryService;

    public AwardController(PrizeQueryService prizeQueryService) {
        this.prizeQueryService = prizeQueryService;
    }

    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.success(Map.of(
                "service", "lottery-award",
                "status", "UP"
        ));
    }

    @GetMapping("/prizes")
    public ApiResponse<List<PrizeView>> listPrizes() {
        return ApiResponse.success(prizeQueryService.listAvailablePrizes());
    }
}
