package com.rcpostflow.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.rcpostflow.service.publisher.PublishException;
import com.rcpostflow.service.publisher.PublishFailureCategory;

class ThreadsClientTest {

    private static final String BASE_URL = "https://threads.invalid";
    private static final String TOKEN = "secret-token";
    private static final String TEXT = "本文全体はログへ出さない";

    @Test
    void sendsCredentialsOutsideUriAndAcceptsResponseWithId() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(BASE_URL + "/me/threads"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> {
                    assertThat(request.getURI().getRawQuery()).isNull();
                    assertThat(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                            .isEqualTo("Bearer " + TOKEN);
                })
                .andRespond(withSuccess("{\"id\":\"creation-123\"}", MediaType.APPLICATION_JSON));

        assertThat(fixture.client().createMediaContainer(TEXT))
                .isEqualTo("creation-123");
        fixture.server().verify();
    }

    @Test
    void rejectsMissingAccessTokenBeforeOpeningExternalConnection() {
        ClientHttpRequestFactory requestFactory = (uri, method) -> {
            throw new AssertionError("外部通信を開始してはいけません");
        };
        RestClient restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(requestFactory)
                .build();
        ThreadsClient client = new ThreadsClient(restClient, " ");

        assertCategory(
                () -> client.createMediaContainer(TEXT),
                PublishFailureCategory.CONFIGURATION_ERROR);
    }

    @ParameterizedTest
    @CsvSource({
        "400, CLIENT_ERROR",
        "429, RATE_LIMITED",
        "503, SERVER_ERROR"
    })
    void classifiesHttpFailures(int status, PublishFailureCategory category) {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(BASE_URL + "/me/threads"))
                .andRespond(withStatus(HttpStatusCode.valueOf(status)));

        assertThatThrownBy(() -> fixture.client().createMediaContainer(TEXT))
                .isInstanceOfSatisfying(PublishException.class, exception -> {
                    assertThat(exception.category()).isEqualTo(category);
                    assertThat(exception.httpStatus()).isEqualTo(status);
                    assertThat(exception.getMessage())
                            .doesNotContain(TOKEN, TEXT, BASE_URL);
                });
        fixture.server().verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "{}",
        "{\"id\":null}",
        "{\"id\":\" \"}",
        "not-json"
    })
    void rejectsNullMissingBlankOrMalformedIdResponse(String responseBody) {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(BASE_URL + "/me/threads"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        assertCategory(
                () -> fixture.client().createMediaContainer(TEXT),
                PublishFailureCategory.INVALID_RESPONSE);
        fixture.server().verify();
    }

    @Test
    void rejectsPublishResponseWithoutId() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(BASE_URL + "/me/threads_publish"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertCategory(
                () -> fixture.client().publishMedia("creation-123"),
                PublishFailureCategory.INVALID_RESPONSE);
        fixture.server().verify();
    }

    @Test
    void classifiesTimeoutWithoutOpeningExternalConnection() {
        ThreadsClient client = clientWithFailure(
                new SocketTimeoutException("read timed out"));

        assertCategory(
                () -> client.createMediaContainer(TEXT),
                PublishFailureCategory.TIMEOUT);
    }

    @Test
    void classifiesCommunicationFailureWithoutOpeningExternalConnection() {
        ThreadsClient client = clientWithFailure(
                new IOException("connection reset"));

        assertCategory(
                () -> client.createMediaContainer(TEXT),
                PublishFailureCategory.COMMUNICATION_ERROR);
    }

    private Fixture fixture() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .build();
        return new Fixture(new ThreadsClient(restClient, TOKEN), server);
    }

    private ThreadsClient clientWithFailure(IOException failure) {
        ClientHttpRequestFactory requestFactory = (uri, method) -> {
            throw failure;
        };
        RestClient restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(requestFactory)
                .build();
        return new ThreadsClient(restClient, TOKEN);
    }

    private void assertCategory(
            ThrowingOperation operation,
            PublishFailureCategory category) {
        assertThatThrownBy(operation::run)
                .isInstanceOfSatisfying(PublishException.class, exception ->
                        assertThat(exception.category()).isEqualTo(category));
    }

    private record Fixture(
            ThreadsClient client,
            MockRestServiceServer server) {
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run();
    }
}
