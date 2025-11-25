package com.sparta.demo4.common.ai.dto;

import java.time.LocalDateTime;

public record WeatherResponse(String city, int temperature, String condition, LocalDateTime timestamp) {
}
