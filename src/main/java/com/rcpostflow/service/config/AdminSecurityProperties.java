package com.rcpostflow.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Component
@Validated
@ConfigurationProperties(prefix = "rc-postflow.security.admin")
public class AdminSecurityProperties {

    @NotBlank(message = "管理者ユーザー名を設定してください")
    private String username;

    @NotBlank(message = "管理者パスワードを設定してください")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
