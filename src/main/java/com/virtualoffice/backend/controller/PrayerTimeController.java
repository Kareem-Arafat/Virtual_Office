package com.virtualoffice.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.virtualoffice.backend.service.PrayerTimeService;

@RestController
@RequestMapping("/api/prayer-times")
public class PrayerTimeController
{
    private final PrayerTimeService prayerTimeService;


    public PrayerTimeController(PrayerTimeService prayerTimeService)
    {
        this.prayerTimeService = prayerTimeService;
    }


    @GetMapping
    public Map<String, Object> getPrayerTimes(@RequestParam double lat,@RequestParam double lon)
    {
        return prayerTimeService.getPrayerTimes(lat, lon);
    }
}