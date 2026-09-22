package com.rcpostflow.scheduler;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.rcpostflow.service.PostService;

@Component
@Profile("!screenshot")
public class PostScheduler {

    private final PostService postService;

    public PostScheduler(PostService postService) {
        this.postService = postService;
    }

    @Scheduled(fixedDelay = 60000)
    public void checkScheduledPosts() {
         postService.executeScheduledPosts();
    }
}
