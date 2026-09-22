package com.rcpostflow.service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ThreadsHttpClientConfig {

    @Bean
    @Qualifier("threadsRestClient")
    RestClient threadsRestClient(
            RestClient.Builder builder,
            ThreadsProperties properties) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        RestClient.Builder configuredBuilder = builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory);

        if (properties.getAccessToken() != null
                && !properties.getAccessToken().isBlank()) {
            configuredBuilder.defaultHeader(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + properties.getAccessToken());
        }

        return configuredBuilder.build();
    }
}
