package com.virtualoffice.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PrayerTimeService
{
    private final String API_URL = "https://api.aladhan.com/v1/timings";



    public Map<String, Object> getPrayerTimes(double latitude, double longitude)
    {
        RestTemplate restTemplate = new RestTemplate();


        String url = API_URL + "?latitude=" + latitude + "&longitude=" + longitude;


        Map<String, Object> response = restTemplate.getForObject(url, Map.class);


        if (response == null)
        {
            throw new RuntimeException("Failed to get prayer times");
        }


        Map<String, Object> data = (Map<String, Object>) response.get("data");
        Map<String, Object> timings = (Map<String, Object>) data.get("timings");
        Map<String, Object> date = (Map<String, Object>) data.get("date");
        Map<String, Object> hijri = (Map<String, Object>) date.get("hijri");
        Map<String, Object> hijriMonth = (Map<String, Object>) hijri.get("month");

        Map<String, Object> result = new HashMap<>();
        

        result.put("Fajr", timings.get("Fajr"));
        result.put("Sunrise", timings.get("Sunrise"));
        result.put("Dhuhr", timings.get("Dhuhr"));
        result.put("Asr", timings.get("Asr"));
        result.put("Maghrib", timings.get("Maghrib"));
        result.put("Isha", timings.get("Isha"));

        result.put("hijriDay", hijri.get("day"));
        result.put("hijriMonth", hijriMonth.get("en"));


        return result;
    }
}