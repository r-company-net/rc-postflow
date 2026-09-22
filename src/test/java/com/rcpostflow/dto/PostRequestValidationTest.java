package com.rcpostflow.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rcpostflow.service.publisher.ThreadsPostText;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class PostRequestValidationTest {

    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Test
    void acceptsValidCreateAndUpdateRequests() {
        assertThat(validator.validate(validCreate())).isEmpty();
        assertThat(validator.validate(validUpdate())).isEmpty();
    }

    @Test
    void requiresTitleBodyScheduledAtAndThreads() {
        CreatePostRequest create = validCreate();
        create.setTitle(" ");
        create.setBody(null);
        create.setScheduledAt(null);
        create.setChannels(List.of());

        UpdatePostRequest update = validUpdate();
        update.setTitle(" ");
        update.setBody(null);
        update.setScheduledAt(null);
        update.setChannels(List.of());

        assertPaths(
                validator.validate(create),
                "title", "body", "scheduledAt", "channels");
        assertPaths(
                validator.validate(update),
                "title", "body", "scheduledAt", "channels");
    }

    @Test
    void rejectsPastScheduledAtForCreateAndUpdate() {
        CreatePostRequest create = validCreate();
        create.setScheduledAt(LocalDateTime.now().minusMinutes(1));
        UpdatePostRequest update = validUpdate();
        update.setScheduledAt(LocalDateTime.now().minusMinutes(1));

        assertPaths(validator.validate(create), "scheduledAt");
        assertPaths(validator.validate(update), "scheduledAt");
    }

    @Test
    void rejectsXAndMultipleChannelsForCreateAndUpdate() {
        CreatePostRequest create = validCreate();
        create.setChannels(List.of("X"));
        UpdatePostRequest update = validUpdate();
        update.setChannels(List.of("THREADS", "X"));

        assertHasPathStartingWith(validator.validate(create), "channels");
        assertHasPathStartingWith(validator.validate(update), "channels");
    }

    @Test
    void validatesTitleAndUrlAgainstPostgresqlColumnLengths() {
        CreatePostRequest create = validCreate();
        create.setTitle("a".repeat(255));
        assertThat(validator.validateProperty(create, "title")).isEmpty();
        create.setTitle("a".repeat(256));
        assertHasPathStartingWith(
                validator.validateProperty(create, "title"), "title");

        UpdatePostRequest update = validUpdate();
        String urlAtLimit = urlWithLength(1024);
        update.setUrl(urlAtLimit);
        assertThat(validator.validateProperty(update, "url")).isEmpty();
        update.setUrl(urlAtLimit + "a");
        assertHasPathStartingWith(
                validator.validateProperty(update, "url"), "url");
    }

    @Test
    void rejectsMalformedUrlButAllowsEmptyUrl() {
        CreatePostRequest create = validCreate();
        create.setUrl("not-a-url");
        assertHasPathStartingWith(validator.validate(create), "url");

        UpdatePostRequest update = validUpdate();
        update.setUrl("");
        assertThat(validator.validate(update)).isEmpty();
    }

    @Test
    void validatesFinalThreadsTextAtFiveHundredCodePoints() {
        String url = "https://example.com";
        int bodyLengthAtLimit = ThreadsPostText.MAX_LENGTH
                - 2
                - ThreadsPostText.length(url);

        CreatePostRequest create = validCreate();
        create.setBody("a".repeat(bodyLengthAtLimit));
        create.setUrl(url);
        assertThat(validator.validate(create)).isEmpty();

        UpdatePostRequest update = validUpdate();
        update.setBody("a".repeat(bodyLengthAtLimit + 1));
        update.setUrl(url);
        assertHasPathStartingWith(
                validator.validate(update), "threadsTextWithinLimit");
    }

    @Test
    void supplementaryCharactersCountAsSingleThreadsCharacters() {
        CreatePostRequest create = validCreate();
        create.setBody("😀".repeat(ThreadsPostText.MAX_LENGTH));
        create.setUrl(null);

        assertThat(validator.validate(create)).isEmpty();

        create.setBody("😀".repeat(ThreadsPostText.MAX_LENGTH + 1));
        assertHasPathStartingWith(
                validator.validate(create), "threadsTextWithinLimit");
    }

    private CreatePostRequest validCreate() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("タイトル");
        request.setBody("本文");
        request.setUrl("https://example.com");
        request.setScheduledAt(LocalDateTime.now().plusDays(1));
        request.setChannels(List.of("THREADS"));
        return request;
    }

    private UpdatePostRequest validUpdate() {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("タイトル");
        request.setBody("本文");
        request.setUrl("https://example.com");
        request.setScheduledAt(LocalDateTime.now().plusDays(1));
        request.setChannels(List.of("THREADS"));
        return request;
    }

    private String urlWithLength(int length) {
        String prefix = "https://example.com/";
        return prefix + "a".repeat(length - prefix.length());
    }

    private void assertPaths(
            Set<? extends ConstraintViolation<?>> violations,
            String... expectedPaths) {
        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(expectedPaths);
    }

    private void assertHasPathStartingWith(
            Set<? extends ConstraintViolation<?>> violations,
            String expectedPath) {
        assertThat(violations).anySatisfy(violation ->
                assertThat(violation.getPropertyPath().toString())
                        .startsWith(expectedPath));
    }
}
