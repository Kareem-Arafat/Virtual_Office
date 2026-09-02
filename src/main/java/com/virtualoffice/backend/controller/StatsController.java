package com.virtualoffice.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtualoffice.backend.dto.StatisticsDTO;
import com.virtualoffice.backend.service.StatisticsService;

@RestController
@RequestMapping("/api/stats")
public class StatsController
{
    private StatisticsService statisticsService;


    public StatsController(StatisticsService statisticsService)
    {
        this.statisticsService = statisticsService;
    }




    @GetMapping("/office")
    public StatisticsDTO getOfficeStats(Authentication authentication)
    {
        String username = authentication.getName();

        return statisticsService.getOfficeStats(username);
    }




    @GetMapping("/me")
    public StatisticsDTO getMyStats(Authentication authentication)
    {
        String username = authentication.getName();

        return statisticsService.getMyStats(username);
    }
}