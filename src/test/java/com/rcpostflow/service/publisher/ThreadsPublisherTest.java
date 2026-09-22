package com.rcpostflow.service.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.rcpostflow.service.client.ThreadsClient;

class ThreadsPublisherTest {

    @Test
    void publishesBodyAndUrlAndReturnsExternalPostId() {
        ThreadsClient client = mock(ThreadsClient.class);
        when(client.createMediaContainer("本文\n\nhttps://example.com"))
                .thenReturn("creation-123");
        when(client.publishMedia("creation-123")).thenReturn("post-456");
        ThreadsPublisher publisher = new ThreadsPublisher(client);

        PublishResult result = publisher.publish(
                new PublishRequest("本文", "https://example.com"));

        assertThat(publisher.channel()).isEqualTo(PublishChannel.THREADS);
        assertThat(result).isEqualTo(
                new PublishResult(PublishChannel.THREADS, "post-456"));
        verify(client).createMediaContainer("本文\n\nhttps://example.com");
        verify(client).publishMedia("creation-123");
    }

    @Test
    void rejectsTextOverThreadsLimitBeforeCallingClient() {
        ThreadsClient client = mock(ThreadsClient.class);
        ThreadsPublisher publisher = new ThreadsPublisher(client);

        assertThatThrownBy(() -> publisher.publish(
                new PublishRequest("a".repeat(501), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("500文字以内");

        verify(client, never()).createMediaContainer(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void validatesLimitUsingFinalBodySeparatorAndUrl() {
        ThreadsClient client = mock(ThreadsClient.class);
        ThreadsPublisher publisher = new ThreadsPublisher(client);
        String url = "https://example.com";
        String bodyAtLimit = "a".repeat(
                ThreadsPostText.MAX_LENGTH - 2 - url.length());
        String finalTextAtLimit = bodyAtLimit + "\n\n" + url;
        when(client.createMediaContainer(finalTextAtLimit))
                .thenReturn("creation-123");
        when(client.publishMedia("creation-123")).thenReturn("post-456");

        publisher.publish(new PublishRequest(bodyAtLimit, url));

        verify(client).createMediaContainer(finalTextAtLimit);

        assertThatThrownBy(() -> publisher.publish(
                new PublishRequest(bodyAtLimit + "a", url)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("501文字");
    }

    @Test
    void countsSupplementaryCharactersAsUnicodeCodePoints() {
        ThreadsClient client = mock(ThreadsClient.class);
        ThreadsPublisher publisher = new ThreadsPublisher(client);
        String text = "😀".repeat(ThreadsPostText.MAX_LENGTH);
        when(client.createMediaContainer(text)).thenReturn("creation-123");
        when(client.publishMedia("creation-123")).thenReturn("post-456");

        PublishResult result = publisher.publish(new PublishRequest(text, null));

        assertThat(result.externalPostId()).isEqualTo("post-456");
        verify(client).createMediaContainer(text);
    }
}
