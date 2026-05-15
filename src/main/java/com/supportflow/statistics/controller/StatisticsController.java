package com.supportflow.statistics.controller;

import com.supportflow.statistics.dto.StatisticsResponse;
import com.supportflow.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/statistics/overview")
    public StatisticsResponse getOverview() {
        return statisticsService.getOverview();
    }

    @GetMapping("/statistics/me")
    public StatisticsResponse getMyStatistics() {
        return statisticsService.getMyStatistics();
    }
}
