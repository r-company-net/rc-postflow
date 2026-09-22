package com.rcpostflow.service.client;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.rcpostflow.service.publisher.PublishChannel;
import com.rcpostflow.service.publisher.PublishException;
import com.rcpostflow.service.publisher.PublishFailureCategory;

@Service
public class ThreadsClient {

    private final RestClient restClient;
    private final String accessToken;

    public ThreadsClient(
            @Qualifier("threadsRestClient") RestClient restClient,
            @Value("${threads.access-token:}") String accessToken) {
        this.restClient = restClient;
        this.accessToken = accessToken;
    }

    public String getMe() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me")
                        .queryParam("fields", "id,username")
                        .build())
                .retrieve()
                .body(String.class);
    }

    public String createMediaContainer(String text) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("media_type", "TEXT");
        form.add("text", text);

        return execute(() -> restClient.post()
                .uri("/me/threads")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(ThreadsCreationResponse.class));
    }

    public String publishMedia(String creationId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("creation_id", creationId);

        return execute(() -> restClient.post()
                .uri("/me/threads_publish")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(ThreadsCreationResponse.class));
    }

    private String execute(Supplier<ThreadsCreationResponse> request) {
        if (accessToken == null || accessToken.isBlank()) {
            throw failure(PublishFailureCategory.CONFIGURATION_ERROR, null);
        }

        try {
            ThreadsCreationResponse response = request.get();

            if (response == null
                    || response.id() == null
                    || response.id().isBlank()) {
                throw failure(PublishFailureCategory.INVALID_RESPONSE, null);
            }

            return response.id();
        } catch (PublishException e) {
            throw e;
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            throw failure(categoryForStatus(status), status);
        } catch (ResourceAccessException e) {
            PublishFailureCategory category = hasTimeoutCause(e)
                    ? PublishFailureCategory.TIMEOUT
                    : PublishFailureCategory.COMMUNICATION_ERROR;
            throw failure(category, null);
        } catch (HttpMessageConversionException | RestClientException e) {
            throw failure(PublishFailureCategory.INVALID_RESPONSE, null);
        }
    }

    private PublishFailureCategory categoryForStatus(int status) {
        if (status == 429) {
            return PublishFailureCategory.RATE_LIMITED;
        }
        if (status >= 400 && status < 500) {
            return PublishFailureCategory.CLIENT_ERROR;
        }
        if (status >= 500 && status < 600) {
            return PublishFailureCategory.SERVER_ERROR;
        }
        return PublishFailureCategory.INVALID_RESPONSE;
    }

    private boolean hasTimeoutCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof HttpTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private PublishException failure(
            PublishFailureCategory category,
            Integer httpStatus) {
        return new PublishException(PublishChannel.THREADS, category, httpStatus);
    }
}
