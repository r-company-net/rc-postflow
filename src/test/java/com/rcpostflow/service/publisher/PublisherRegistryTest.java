package com.rcpostflow.service.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

class PublisherRegistryTest {

    @Test
    void resolvesPublisherByChannel() {
        Publisher publisher = publisherFor(PublishChannel.THREADS);
        PublisherRegistry registry = new PublisherRegistry(List.of(publisher));

        assertThat(registry.getRequired(PublishChannel.THREADS)).isSameAs(publisher);
    }

    @Test
    void rejectsDuplicatePublishersForSameChannel() {
        Publisher first = publisherFor(PublishChannel.THREADS);
        Publisher second = publisherFor(PublishChannel.THREADS);

        assertThatThrownBy(() -> new PublisherRegistry(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("THREADS");
    }

    @Test
    void rejectsUnsupportedChannel() {
        PublisherRegistry registry = new PublisherRegistry(List.of(
                publisherFor(PublishChannel.THREADS)));

        assertThatThrownBy(() -> registry.getRequired(PublishChannel.X))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("X");
    }

    private Publisher publisherFor(PublishChannel channel) {
        Publisher publisher = mock(Publisher.class);
        when(publisher.channel()).thenReturn(channel);
        return publisher;
    }
}
