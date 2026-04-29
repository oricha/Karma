package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.dto.BlogDtos;
import com.karma.platform.service.BlogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

    private final BlogService blogService;
    private final CurrentUser currentUser;

    public BlogController(BlogService blogService, CurrentUser currentUser) {
        this.blogService = blogService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<BlogDtos.BlogPostResponse> list() {
        return blogService.listPublished();
    }

    @GetMapping("/featured")
    public List<BlogDtos.BlogPostResponse> featured() {
        return blogService.featured();
    }

    @GetMapping("/{slug}")
    public BlogDtos.BlogPostResponse detail(@PathVariable String slug) {
        return blogService.detail(slug);
    }

    @PostMapping
    public BlogDtos.BlogPostResponse create(@RequestBody BlogDtos.UpsertBlogPostRequest request) {
        return blogService.create(currentUser.id(), request);
    }

    @PutMapping("/{id}")
    public BlogDtos.BlogPostResponse update(@PathVariable String id, @RequestBody BlogDtos.UpsertBlogPostRequest request) {
        return blogService.update(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        blogService.delete(currentUser.id(), id);
    }
}
