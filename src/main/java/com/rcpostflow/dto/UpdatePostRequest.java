package com.rcpostflow.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.rcpostflow.service.publisher.ThreadsPostText;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public class UpdatePostRequest {

    @NotBlank(message = "タイトルは必須です")
    @Size(max = 255, message = "タイトルは255文字以内で入力してください")
    private String title;

    @NotBlank(message = "本文は必須です")
    private String body;

    @Size(max = 1024, message = "URLは1024文字以内で入力してください")
    @URL(message = "URLは正しい形式で入力してください")
    private String url;

    @NotNull(message = "投稿日は必須です")
    @Future(message = "投稿日は未来の日時を指定してください")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduledAt;

    @NotEmpty(message = "投稿先にThreadsを選択してください")
    @Size(max = 1, message = "投稿先はThreadsのみ選択できます")
    private List<@Pattern(
            regexp = "THREADS",
            message = "投稿先はThreadsのみ選択できます") String> channels =
            new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    @AssertTrue(message = "本文とURLを合わせたThreads投稿は500文字以内にしてください")
    public boolean isThreadsTextWithinLimit() {
        return ThreadsPostText.isWithinLimit(body, url);
    }
}
