package com.karma.platform.controller;

import com.karma.platform.dto.BlogDtos;
import com.karma.platform.service.BlogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public List<BlogDtos.BlogPostResponse> list() {
        return blogService.list();
    }

    @GetMapping("/featured")
    public List<BlogDtos.BlogPostResponse> featured() {
        return blogService.list().stream().limit(3).toList();
    }

    @GetMapping("/{slug}")
    public BlogDtos.BlogPostResponse detail(@PathVariable String slug) {
        return blogService.detail(slug);
    }
}
