package com.rcpostflow.service.publisher;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PublisherRegistry {

    private final Map<PublishChannel, Publisher> publishers;

    public PublisherRegistry(List<Publisher> publishers) {
        EnumMap<PublishChannel, Publisher> registeredPublishers =
                new EnumMap<>(PublishChannel.class);

        for (Publisher publisher : publishers) {
            Publisher existing = registeredPublishers.putIfAbsent(
                    publisher.channel(),
                    publisher);

            if (existing != null) {
                throw new IllegalStateException(
                        "Publisherが重複しています: " + publisher.channel());
            }
        }

        this.publishers = Collections.unmodifiableMap(registeredPublishers);
    }

    public Publisher getRequired(PublishChannel channel) {
        Publisher publisher = publishers.get(channel);

        if (publisher == null) {
            throw new UnsupportedOperationException(
                    "未対応の投稿先チャネルです: " + channel);
        }

        return publisher;
    }
}
