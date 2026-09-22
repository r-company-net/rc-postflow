package com.rcpostflow.service.publisher;

public record PublishRequest(
        String body,
        String url
) {
}
