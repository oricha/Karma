package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.BlogDtos;
import com.karma.platform.persistence.repository.BlogPostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final ApiMapper apiMapper;

    public BlogService(BlogPostRepository blogPostRepository, ApiMapper apiMapper) {
        this.blogPostRepository = blogPostRepository;
        this.apiMapper = apiMapper;
    }

    public List<BlogDtos.BlogPostResponse> list() {
        return blogPostRepository.findAll().stream()
                .sorted(Comparator.comparing(item -> item.getPublishedAt(), Comparator.reverseOrder()))
                .map(apiMapper::toBlogPost)
                .toList();
    }

    public BlogDtos.BlogPostResponse detail(String slug) {
        return blogPostRepository.findBySlug(slug)
                .map(apiMapper::toBlogPost)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.blog-post-not-found", "Blog post not found"));
    }
}
