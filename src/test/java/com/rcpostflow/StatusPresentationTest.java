package com.rcpostflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.rcpostflow.entity.Post;
import com.rcpostflow.entity.PostStatus;
import com.rcpostflow.repository.PostRepository;
import com.rcpostflow.scheduler.PostScheduler;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", authorities = "ADMIN")
class StatusPresentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    @Transactional
    void rendersEachStatusWithItsReleaseOneLabelAndFilter() throws Exception {
        for (PostStatus status : PostStatus.values()) {
            postRepository.save(post(status));
        }
        postRepository.flush();

        String html = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(statusLabel(html, PostStatus.PENDING)).contains("予約済み");
        assertThat(statusLabel(html, PostStatus.SCHEDULED)).contains("投稿処理中");
        assertThat(statusLabel(html, PostStatus.POSTED)).contains("投稿済み");
        assertThat(statusLabel(html, PostStatus.ERROR)).contains("投稿失敗");

        assertThat(html)
                .contains("<option value=\"PENDING\">予約済み</option>")
                .contains("<option value=\"SCHEDULED\">投稿処理中</option>")
                .contains("<option value=\"POSTED\">投稿済み</option>")
                .contains("<option value=\"ERROR\">投稿失敗</option>");
    }

    @Test
    void reservationKpiCountsPendingAndExcludesScheduled() throws IOException {
        String script = new ClassPathResource("static/js/app.js")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(script)
                .contains("setKpi(\"kpi-scheduled\", \"PENDING\")")
                .doesNotContain("setKpi(\"kpi-scheduled\", \"SCHEDULED\")")
                .contains("setKpi(\"kpi-posted\", \"POSTED\")")
                .contains("setKpi(\"kpi-error\", \"ERROR\")");
    }

    @Test
    void screenshotProfileDoesNotCreateScheduler() {
        new ApplicationContextRunner()
                .withInitializer(context -> context.getEnvironment()
                        .setActiveProfiles("screenshot"))
                .withUserConfiguration(PostScheduler.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(PostScheduler.class));
    }

    private String statusLabel(String html, PostStatus status) {
        Pattern rowPattern = Pattern.compile(
                "<tr class=\"post-row\"[^>]*data-status=\""
                        + status.name()
                        + "\"[^>]*>(.*?)</tr>",
                Pattern.DOTALL);
        Matcher matcher = rowPattern.matcher(html);
        assertThat(matcher.find())
                .as("row for status %s", status)
                .isTrue();
        return matcher.group(1);
    }

    private Post post(PostStatus status) {
        Post post = new Post();
        post.setTitle("表示確認 " + status.name());
        post.setBody("状態表示の回帰テスト用本文");
        post.setUrl("https://example.com/" + status.name().toLowerCase());
        post.setScheduledAt(LocalDateTime.now().plusDays(1));
        post.setChannels("THREADS");
        post.setStatus(status);
        return post;
    }
}
