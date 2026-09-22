package com.rcpostflow;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

import com.rcpostflow.entity.Post;
import com.rcpostflow.entity.PostStatus;
import com.rcpostflow.repository.PostRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", authorities = "ADMIN")
class RcPostflowApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

	@Test
	void contextLoads() {
	}

    @Test
    @Transactional
    void editPageRendersPathBasedActionWithoutEditableId() throws Exception {
        Post post = new Post();
        post.setTitle("タイトル");
        post.setBody("本文");
        post.setScheduledAt(LocalDateTime.of(2026, 9, 6, 12, 30));
        post.setChannels("THREADS");
        post.setStatus(PostStatus.POSTED);
        Post saved = postRepository.saveAndFlush(post);

        mockMvc.perform(get("/posts/{id}/edit", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "action=\"/posts/" + saved.getId() + "\"")))
                .andExpect(content().string(not(containsString("name=\"id\""))))
                .andExpect(content().string(containsString("value=\"THREADS\"")))
                .andExpect(content().string(containsString("checked=\"checked\"")))
                .andExpect(content().string(containsString("X（未サポート）")))
                .andExpect(content().string(not(containsString("value=\"X\""))))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString(
                        "action=\"/posts/" + saved.getId() + "/delete\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void validationMessageIsRenderedForUnsupportedChannel() throws Exception {
        String futureDate = LocalDateTime.now()
                .plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        mockMvc.perform(post("/posts").with(csrf())
                        .param("title", "タイトル")
                        .param("body", "本文")
                        .param("scheduledAt", futureDate)
                        .param("channels", "X"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "投稿先はThreadsのみ選択できます")));
    }

}
