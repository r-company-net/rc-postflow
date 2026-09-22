package com.rcpostflow.service.publisher;

public enum PublishFailureCategory {
    CONFIGURATION_ERROR,
    CLIENT_ERROR,
    RATE_LIMITED,
    SERVER_ERROR,
    TIMEOUT,
    COMMUNICATION_ERROR,
    INVALID_RESPONSE
}
