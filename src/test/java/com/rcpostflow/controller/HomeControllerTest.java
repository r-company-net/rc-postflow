package com.rcpostflow.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.rcpostflow.dto.CreatePostRequest;
import com.rcpostflow.dto.UpdatePostRequest;
import com.rcpostflow.entity.Post;
import com.rcpostflow.service.PostService;

class HomeControllerTest {

    private static final DateTimeFormatter FORM_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private PostService postService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        postService = mock(PostService.class);
        when(postService.findAll()).thenReturn(List.of());
        mockMvc = MockMvcBuilders
                .standaloneSetup(new HomeController(postService))
                .build();
    }

    @Test
    void createIgnoresInjectedIdAndStatusFields() throws Exception {
        String futureDate = futureDate();

        mockMvc.perform(post("/posts")
                        .param("id", "999")
                        .param("status", "POSTED")
                        .param("title", "タイトル")
                        .param("body", "本文")
                        .param("url", "https://example.com")
                        .param("scheduledAt", futureDate)
                        .param("channels", "THREADS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        ArgumentCaptor<CreatePostRequest> captor =
                ArgumentCaptor.forClass(CreatePostRequest.class);
        verify(postService).create(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("タイトル");
        assertThat(captor.getValue().getChannels())
                .containsExactly("THREADS");
    }

    @Test
    void updateUsesPathIdAndIgnoresInjectedIdAndStatusFields() throws Exception {
        when(postService.update(eq(7L), any(UpdatePostRequest.class)))
                .thenReturn(Optional.of(new Post()));

        mockMvc.perform(post("/posts/7")
                        .param("id", "999")
                        .param("status", "PENDING")
                        .param("title", "更新タイトル")
                        .param("body", "更新本文")
                        .param("scheduledAt", futureDate())
                        .param("channels", "THREADS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        ArgumentCaptor<UpdatePostRequest> captor =
                ArgumentCaptor.forClass(UpdatePostRequest.class);
        verify(postService).update(eq(7L), captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("更新タイトル");
        assertThat(captor.getValue().getChannels())
                .containsExactly("THREADS");
    }

    @Test
    void missingEditIdShowsUserFacingErrorInsteadOfServerError() throws Exception {
        when(postService.findById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/posts/404/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute(
                        "errorMessage",
                        "指定された投稿が見つかりません。"))
                .andExpect(model().attribute("editing", false));
    }

    @Test
    void missingUpdateIdShowsUserFacingErrorInsteadOfServerError() throws Exception {
        when(postService.update(eq(404L), any(UpdatePostRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/posts/404")
                        .param("title", "更新タイトル")
                        .param("body", "更新本文")
                        .param("scheduledAt", futureDate())
                        .param("channels", "THREADS"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute(
                        "errorMessage",
                        "指定された投稿が見つかりません。"))
                .andExpect(model().attribute("editing", true));
    }

    @Test
    void validationErrorRedisplaysCreateFormWithoutSaving() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("title", "")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("editing", false));

        verify(postService, never()).create(any(CreatePostRequest.class));
    }

    @Test
    void unsupportedXChannelRedisplaysFormWithoutSaving() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("title", "タイトル")
                        .param("body", "本文")
                        .param("scheduledAt", futureDate())
                        .param("channels", "X"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasErrors("post"));

        verify(postService, never()).create(any(CreatePostRequest.class));
    }

    @Test
    void deleteIsAvailableOnlyAsPost() throws Exception {
        mockMvc.perform(post("/posts/7/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(postService).deleteById(7L);

        mockMvc.perform(get("/posts/7/delete"))
                .andExpect(status().isMethodNotAllowed());
    }

    private String futureDate() {
        return LocalDateTime.now().plusDays(1).format(FORM_DATE_TIME);
    }
}
