package com.rcpostflow.service.publisher;

import java.util.Locale;

public enum PublishChannel {
    X,
    THREADS;

    public static PublishChannel from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("投稿先チャネルは必須です");
        }

        try {
            return valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("未対応の投稿先チャネルです: " + value, exception);
        }
    }
}
