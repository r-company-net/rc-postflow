package com.rcpostflow.service.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class AdminSecurityPropertiesTest {

    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Test
    void rejectsMissingAdministratorCredentials() {
        AdminSecurityProperties properties = new AdminSecurityProperties();

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("username", "password");
    }

    @Test
    void applicationContextFailsWhenAdministratorCredentialsAreMissing() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        ConfigurationPropertiesAutoConfiguration.class,
                        ValidationAutoConfiguration.class))
                .withUserConfiguration(PropertiesConfiguration.class)
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNotNull();
                    assertThat(rootCause(context.getStartupFailure()).getMessage())
                            .contains(
                                    "管理者ユーザー名を設定してください",
                                    "管理者パスワードを設定してください");
                });
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AdminSecurityProperties.class)
    static class PropertiesConfiguration {
    }
}
