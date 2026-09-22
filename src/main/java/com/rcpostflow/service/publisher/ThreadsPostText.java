package com.rcpostflow.service.publisher;

public final class ThreadsPostText {

    public static final int MAX_LENGTH = 500;

    private ThreadsPostText() {
    }

    public static String build(String body, String url) {
        StringBuilder text = new StringBuilder();

        if (body != null && !body.isBlank()) {
            text.append(body);
        }

        if (url != null && !url.isBlank()) {
            if (!text.isEmpty()) {
                text.append("\n\n");
            }
            text.append(url);
        }

        return text.toString();
    }

    public static int length(String text) {
        return text.codePointCount(0, text.length());
    }

    public static boolean isWithinLimit(String body, String url) {
        return length(build(body, url)) <= MAX_LENGTH;
    }
}
