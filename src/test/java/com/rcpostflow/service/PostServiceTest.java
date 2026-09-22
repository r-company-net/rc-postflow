package com.rcpostflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import com.rcpostflow.dto.CreatePostRequest;
import com.rcpostflow.dto.UpdatePostRequest;
import com.rcpostflow.entity.Post;
import com.rcpostflow.entity.PostStatus;
import com.rcpostflow.repository.PostRepository;
import com.rcpostflow.service.publisher.PublishChannel;
import com.rcpostflow.service.publisher.PublishException;
import com.rcpostflow.service.publisher.PublishFailureCategory;
import com.rcpostflow.service.publisher.PublishRequest;
import com.rcpostflow.service.publisher.PublishResult;
import com.rcpostflow.service.publisher.Publisher;
import com.rcpostflow.service.publisher.PublisherRegistry;

class PostServiceTest {

    @Test
    void createsPostWithGeneratedIdAndPendingStatus() {
        PostRepository repository = mock(PostRepository.class);
        PublisherRegistry registry = mock(PublisherRegistry.class);
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("タイトル");
        request.setBody("本文");
        request.setUrl("https://example.com");
        request.setScheduledAt(LocalDateTime.of(2026, 9, 6, 12, 30));
        request.setChannels(List.of("THREADS"));
        when(repository.save(any(Post.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Post created = new PostService(repository, registry).create(request);

        assertThat(created.getId()).isNull();
        assertThat(created.getStatus()).isEqualTo(PostStatus.PENDING);
        assertThat(created.getTitle()).isEqualTo("タイトル");
        assertThat(created.getBody()).isEqualTo("本文");
        assertThat(created.getUrl()).isEqualTo("https://example.com");
        assertThat(created.getScheduledAt())
                .isEqualTo(LocalDateTime.of(2026, 9, 6, 12, 30));
        assertThat(created.getChannels()).isEqualTo("THREADS");
    }

    @ParameterizedTest
    @EnumSource(value = PostStatus.class, names = {"POSTED", "ERROR"})
    void updatesOnlyEditableFieldsAndPreservesIdentityAndStatus(
            PostStatus existingStatus) {
        PostRepository repository = mock(PostRepository.class);
        PublisherRegistry registry = mock(PublisherRegistry.class);
        Post existing = scheduledPost("X");
        existing.setId(42L);
        existing.setStatus(existingStatus);
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("更新タイトル");
        request.setBody("更新本文");
        request.setUrl("https://updated.example.com");
        request.setScheduledAt(LocalDateTime.of(2026, 9, 7, 8, 15));
        request.setChannels(List.of("THREADS"));
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Optional<Post> updated = new PostService(repository, registry)
                .update(42L, request);

        assertThat(updated).containsSame(existing);
        assertThat(existing.getId()).isEqualTo(42L);
        assertThat(existing.getStatus()).isEqualTo(existingStatus);
        assertThat(existing.getTitle()).isEqualTo("更新タイトル");
        assertThat(existing.getBody()).isEqualTo("更新本文");
        assertThat(existing.getUrl()).isEqualTo("https://updated.example.com");
        assertThat(existing.getScheduledAt())
                .isEqualTo(LocalDateTime.of(2026, 9, 7, 8, 15));
        assertThat(existing.getChannels()).isEqualTo("THREADS");
        verify(repository).save(existing);
    }

    @Test
    void doesNotSaveUpdateWhenPostDoesNotExist() {
        PostRepository repository = mock(PostRepository.class);
        PublisherRegistry registry = mock(PublisherRegistry.class);
        UpdatePostRequest request = new UpdatePostRequest();
        when(repository.findById(404L)).thenReturn(Optional.empty());

        Optional<Post> updated = new PostService(repository, registry)
                .update(404L, request);

        assertThat(updated).isEmpty();
        verify(repository, never()).save(any(Post.class));
    }

    @Test
    void publishesThroughRegistryWithoutDependingOnConcreteAdapter() {
        PostRepository repository = mock(PostRepository.class);
        PublisherRegistry registry = mock(PublisherRegistry.class);
        Publisher publisher = mock(Publisher.class);
        Post post = scheduledPost("THREADS");
        when(repository.findByStatusAndScheduledAtLessThanEqual(
                eq(PostStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(post));
        when(registry.getRequired(PublishChannel.THREADS)).thenReturn(publisher);
        when(publisher.publish(any(PublishRequest.class)))
                .thenReturn(new PublishResult(PublishChannel.THREADS, "post-456"));

        new PostService(repository, registry).executeScheduledPosts();

        ArgumentCaptor<PublishRequest> requestCaptor =
                ArgumentCaptor.forClass(PublishRequest.class);
        verify(publisher).publish(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .isEqualTo(new PublishRequest("本文", "https://example.com"));
        assertThat(post.getStatus()).isEqualTo(PostStatus.POSTED);
    }

    @Test
    void marksPostAsErrorWhenSelectedChannelHasNoAdapter() {
        PostRepository repository = mock(PostRepository.class);
        PublisherRegistry registry = mock(PublisherRegistry.class);
        Post post = scheduledPost("X");
        when(repository.findByStatusAndScheduledAtLessThanEqual(
                eq(PostStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(post));
        when(registry.getRequired(PublishChannel.X))
                .thenThrow(new UnsupportedOperationException("X is not supported"));

        new PostService(repository, registry).executeScheduledPosts();

        assertThat(post.getStatus()).isEqualTo(PostStatus.ERROR);
        verify(repository, times(2)).save(post);
    }

    @Test
    void marksPostAsErrorWithoutRetryWhenPublisherFails() {
        PostRepository repository = mock(PostRepository.class);
        PublisherRegistry registry = mock(PublisherRegistry.class);
        Publisher publisher = mock(Publisher.class);
        Post post = scheduledPost("THREADS");
        when(repository.findByStatusAndScheduledAtLessThanEqual(
                eq(PostStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(post));
        when(registry.getRequired(PublishChannel.THREADS)).thenReturn(publisher);
        when(publisher.publish(any(PublishRequest.class)))
                .thenThrow(new PublishException(
                        PublishChannel.THREADS,
                        PublishFailureCategory.TIMEOUT,
                        null));

        new PostService(repository, registry).executeScheduledPosts();

        assertThat(post.getStatus()).isEqualTo(PostStatus.ERROR);
        verify(publisher, times(1)).publish(any(PublishRequest.class));
        verify(repository, times(2)).save(post);
    }

    private Post scheduledPost(String channels) {
        Post post = new Post();
        post.setTitle("タイトル");
        post.setBody("本文");
        post.setUrl("https://example.com");
        post.setScheduledAt(LocalDateTime.now().minusMinutes(1));
        post.setChannels(channels);
        return post;
    }
}
