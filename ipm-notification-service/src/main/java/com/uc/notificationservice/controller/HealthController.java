package com.uc.notificationservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** Simple health endpoint for smoke checks. */
@RestController
public class HealthController {
    @GetMapping("/api/health")
    public Map<String, String> health(){ return Map.of("status","ok"); }
}
