package com.rcpostflow.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.rcpostflow.dto.CreatePostRequest;
import com.rcpostflow.dto.UpdatePostRequest;
import com.rcpostflow.entity.Post;
import com.rcpostflow.service.PostService;

import jakarta.validation.Valid;

@Controller
public class HomeController {

    private final PostService postService;

    public HomeController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("post", new CreatePostRequest());
        preparePage(model, false, null);
        return "index";
    }

    @InitBinder("post")
    public void bindPostFields(WebDataBinder binder) {
        binder.setAllowedFields(
                "title",
                "body",
                "url",
                "scheduledAt",
                "channels");
    }

    @PostMapping("/posts")
    public String createPost(
            @Valid @ModelAttribute("post") CreatePostRequest post,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            preparePage(model, false, null);
            return "index";
        }

        postService.create(post);
        return "redirect:/";
    }

    @GetMapping("/posts/{id}/edit")
    public String editPost(@PathVariable Long id, Model model) {
        Optional<Post> existingPost = postService.findById(id);

        if (existingPost.isEmpty()) {
            model.addAttribute("post", new CreatePostRequest());
            model.addAttribute("errorMessage", "指定された投稿が見つかりません。");
            preparePage(model, false, null);
            return "index";
        }

        model.addAttribute("post", toUpdateRequest(existingPost.get()));
        preparePage(model, true, id);
        return "index";
    }

    @PostMapping("/posts/{id}")
    public String updatePost(
            @PathVariable Long id,
            @Valid @ModelAttribute("post") UpdatePostRequest post,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            preparePage(model, true, id);
            return "index";
        }

        if (postService.update(id, post).isEmpty()) {
            model.addAttribute("errorMessage", "指定された投稿が見つかりません。");
            preparePage(model, true, id);
            return "index";
        }

        return "redirect:/";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id) {

        postService.deleteById(id);

        return "redirect:/";
    }

    private void preparePage(Model model, boolean editing, Long postId) {
        model.addAttribute("title", "RC-PostFlow");
        model.addAttribute("message",
                "SNS運用を「作業」から「仕組み」に変えるコンテンツ配信基盤");
        model.addAttribute("posts", postService.findAll());
        model.addAttribute("editing", editing);
        model.addAttribute("postId", postId);
    }

    private UpdatePostRequest toUpdateRequest(Post post) {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle(post.getTitle());
        request.setBody(post.getBody());
        request.setUrl(post.getUrl());
        request.setScheduledAt(post.getScheduledAt());
        request.setChannels(splitChannels(post.getChannels()));
        return request;
    }

    private List<String> splitChannels(String channels) {
        return channels == null || channels.isBlank()
                ? List.of()
                : Arrays.stream(channels.split(","))
                        .map(String::strip)
                        .toList();
    }
}

