package com.rcpostflow.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.rcpostflow.entity.Post;
import com.rcpostflow.entity.PostStatus;
import com.rcpostflow.repository.PostRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    private static final DateTimeFormatter FORM_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    void unauthenticatedUserCannotReadManagementPages() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"))
                .andExpect(unauthenticated());

        mockMvc.perform(get("/posts/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void unauthenticatedUserCannotCreateUpdateOrDelete() throws Exception {
        mockMvc.perform(validCreateRequest().with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(post("/posts/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(post("/posts/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "test-admin", authorities = "ADMIN")
    void authenticatedAdministratorCanReadManagementPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("test-admin"));
    }

    @Test
    @WithMockUser(username = "test-admin", authorities = "ADMIN")
    void csrfIsRequiredForCreateUpdateAndDelete() throws Exception {
        mockMvc.perform(validCreateRequest())
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/posts/1"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/posts/1/delete"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(username = "test-admin", authorities = "ADMIN")
    void deletionRequiresPostAndCsrf() throws Exception {
        Post saved = postRepository.saveAndFlush(postForDeletion());

        mockMvc.perform(get("/posts/{id}/delete", saved.getId()))
                .andExpect(status().isMethodNotAllowed());
        assertThat(postRepository.existsById(saved.getId())).isTrue();

        mockMvc.perform(post("/posts/{id}/delete", saved.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        assertThat(postRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    @WithMockUser(username = "test-admin", authorities = "ADMIN")
    void threadsDiagnosticEndpointIsRemoved() throws Exception {
        mockMvc.perform(get("/threads/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    void logoutPreventsFurtherManagementAccess() throws Exception {
        MvcResult loginResult = mockMvc.perform(formLogin()
                        .user("test-admin")
                        .password("test-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("test-admin"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult
                .getRequest()
                .getSession(false);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(unauthenticated());
        assertThat(session.isInvalid()).isTrue();

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
            validCreateRequest() {
        return post("/posts")
                .param("title", "タイトル")
                .param("body", "本文")
                .param("scheduledAt", LocalDateTime.now()
                        .plusDays(1)
                        .format(FORM_DATE_TIME))
                .param("channels", "THREADS");
    }

    private Post postForDeletion() {
        Post post = new Post();
        post.setTitle("削除対象");
        post.setBody("本文");
        post.setScheduledAt(LocalDateTime.now().plusDays(1));
        post.setChannels("THREADS");
        post.setStatus(PostStatus.POSTED);
        return post;
    }
}
