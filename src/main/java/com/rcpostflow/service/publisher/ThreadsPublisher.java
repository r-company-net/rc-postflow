package com.rcpostflow.service.publisher;

import com.rcpostflow.service.client.ThreadsClient;
import org.springframework.stereotype.Component;

@Component
public class ThreadsPublisher implements Publisher {

    private final ThreadsClient threadsClient;
    public ThreadsPublisher(ThreadsClient threadsClient) {
        this.threadsClient = threadsClient;
    }

    @Override
    public PublishChannel channel() {
        return PublishChannel.THREADS;
    }

    @Override
    public PublishResult publish(PublishRequest request) {

        String text = ThreadsPostText.build(request.body(), request.url());
        int textLength = ThreadsPostText.length(text);

        if (textLength > ThreadsPostText.MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Threads投稿文は500文字以内にしてください。現在: "
                            + textLength
                            + "文字"
            );
        }

        String creationId = threadsClient.createMediaContainer(text);

        String externalPostId = threadsClient.publishMedia(creationId);

        return new PublishResult(channel(), externalPostId);
    }
}
