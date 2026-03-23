package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.BlogDtos;
import com.karma.platform.seed.PlatformDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogService {

    private final PlatformDataStore dataStore;
    private final ApiMapper apiMapper;

    public BlogService(PlatformDataStore dataStore, ApiMapper apiMapper) {
        this.dataStore = dataStore;
        this.apiMapper = apiMapper;
    }

    public List<BlogDtos.BlogPostResponse> list() {
        return dataStore.blogPosts().stream().map(apiMapper::toBlogPost).toList();
    }

    public BlogDtos.BlogPostResponse detail(String slug) {
        return dataStore.blogPostBySlug(slug).map(apiMapper::toBlogPost)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Blog post not found"));
    }
}
