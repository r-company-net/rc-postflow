package com.rcpostflow.service;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rcpostflow.dto.CreatePostRequest;
import com.rcpostflow.dto.UpdatePostRequest;
import com.rcpostflow.entity.Post;
import com.rcpostflow.entity.PostStatus;
import com.rcpostflow.repository.PostRepository;
import com.rcpostflow.service.publisher.PublishChannel;
import com.rcpostflow.service.publisher.PublishException;
import com.rcpostflow.service.publisher.PublishRequest;
import com.rcpostflow.service.publisher.PublishResult;
import com.rcpostflow.service.publisher.Publisher;
import com.rcpostflow.service.publisher.PublisherRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PostService {
    
    private final PostRepository postRepository;
    private final PublisherRegistry publisherRegistry;
    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    public PostService(
            PostRepository postRepository,
            PublisherRegistry publisherRegistry) {
        this.postRepository = postRepository;
        this.publisherRegistry = publisherRegistry;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post create(CreatePostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setBody(request.getBody());
        post.setUrl(request.getUrl());
        post.setScheduledAt(request.getScheduledAt());
        post.setChannels(joinChannels(request.getChannels()));
        post.setStatus(PostStatus.PENDING);

        return postRepository.save(post);
    }

    public Optional<Post> update(Long id, UpdatePostRequest request) {
        return postRepository.findById(id).map(post -> {
            post.setTitle(request.getTitle());
            post.setBody(request.getBody());
            post.setUrl(request.getUrl());
            post.setScheduledAt(request.getScheduledAt());
            post.setChannels(joinChannels(request.getChannels()));
            return postRepository.save(post);
        });
    }
    
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }

    private String joinChannels(List<String> channels) {
        return channels == null || channels.isEmpty()
                ? null
                : String.join(",", channels);
    }
    
    public void executeScheduledPosts() {
        LocalDateTime now = LocalDateTime.now();

        List<Post> posts = postRepository
                .findByStatusAndScheduledAtLessThanEqual(
                        PostStatus.PENDING,
                        now);

        log.info("投稿対象件数：{}", posts.size());

        for (Post post : posts) {
            log.info("投稿対象：{}", post.getTitle());

            publish(post);
        }
    }
    
    private void publish(Post post) {

        post.setStatus(PostStatus.SCHEDULED);
        postRepository.save(post);

        try {

            log.info("投稿処理開始：{}", post.getTitle());

            publishToSelectedChannels(post);

            post.setStatus(PostStatus.POSTED);

        } catch (PublishException e) {

            post.setStatus(PostStatus.ERROR);

            log.error(
                    "投稿エラー：{} channel={} category={} httpStatus={}",
                    post.getTitle(),
                    e.channel(),
                    e.category(),
                    e.httpStatus());
        } catch (Exception e) {

            post.setStatus(PostStatus.ERROR);

            log.error(
                    "投稿エラー：{} exceptionType={}",
                    post.getTitle(),
                    e.getClass().getSimpleName());
        }

        postRepository.save(post);

        log.info("投稿処理完了：{} status={}", post.getTitle(), post.getStatus());
    }

    private void publishToSelectedChannels(Post post) {
        if (post.getChannels() == null || post.getChannels().isBlank()) {
            return;
        }

        PublishRequest request = new PublishRequest(post.getBody(), post.getUrl());

        Arrays.stream(post.getChannels().split(","))
                .map(PublishChannel::from)
                .forEach(channel -> publish(channel, request));
    }

    private void publish(PublishChannel channel, PublishRequest request) {
        Publisher publisher = publisherRegistry.getRequired(channel);
        PublishResult result = publisher.publish(request);

        if (result.channel() != channel) {
            throw new IllegalStateException(
                    "Publisherの応答チャネルが一致しません: expected="
                            + channel
                            + " actual="
                            + result.channel());
        }

        log.info(
                "チャネル投稿完了：channel={} externalPostId={}",
                result.channel(),
                result.externalPostId());
    }
}

