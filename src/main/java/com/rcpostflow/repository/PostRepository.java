package com.rcpostflow.repository;
import com.rcpostflow.entity.Post;
import com.rcpostflow.entity.PostStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PostRepository extends JpaRepository<Post, Long> {
        List<Post> findByStatusAndScheduledAtLessThanEqual(
        PostStatus status,
        LocalDateTime scheduledAt
    );
}
