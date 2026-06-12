package com.example.template.messaging;

public record ItemEvent(String eventType, String itemId, String name, String description) {
}
