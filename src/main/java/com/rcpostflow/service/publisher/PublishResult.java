package com.rcpostflow.service.publisher;

import java.util.Objects;

public record PublishResult(
        PublishChannel channel,
        String externalPostId
) {
    public PublishResult {
        Objects.requireNonNull(channel, "channel must not be null");
        if (externalPostId == null || externalPostId.isBlank()) {
            throw new IllegalArgumentException("externalPostId must not be blank");
        }
    }
}
