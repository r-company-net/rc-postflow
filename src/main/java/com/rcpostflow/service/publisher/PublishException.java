package com.rcpostflow.service.publisher;

public class PublishException extends RuntimeException {

    private final PublishChannel channel;
    private final PublishFailureCategory category;
    private final Integer httpStatus;

    public PublishException(
            PublishChannel channel,
            PublishFailureCategory category,
            Integer httpStatus) {
        super(buildMessage(channel, category, httpStatus));
        this.channel = channel;
        this.category = category;
        this.httpStatus = httpStatus;
    }

    public PublishChannel channel() {
        return channel;
    }

    public PublishFailureCategory category() {
        return category;
    }

    public Integer httpStatus() {
        return httpStatus;
    }

    private static String buildMessage(
            PublishChannel channel,
            PublishFailureCategory category,
            Integer httpStatus) {
        String message = "Publisher failed: channel=" + channel
                + " category=" + category;
        return httpStatus == null
                ? message
                : message + " httpStatus=" + httpStatus;
    }
}
