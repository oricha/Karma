package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.BlogDtos;
import com.karma.platform.model.UserRole;
import com.karma.platform.persistence.entity.BlogPostEntity;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.repository.BlogPostRepository;
import com.karma.platform.persistence.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;
    private final ApiMapper apiMapper;

    public BlogService(BlogPostRepository blogPostRepository, UserRepository userRepository, ApiMapper apiMapper) {
        this.blogPostRepository = blogPostRepository;
        this.userRepository = userRepository;
        this.apiMapper = apiMapper;
    }

    public List<BlogDtos.BlogPostResponse> listPublished() {
        return blogPostRepository.findByPublishedTrueOrderByPublishedAtDesc().stream()
                .map(apiMapper::toBlogPost)
                .toList();
    }

    public List<BlogDtos.BlogPostResponse> featured() {
        return blogPostRepository.findTop3ByPublishedTrueAndFeaturedTrueOrderByPublishedAtDesc().stream()
                .map(apiMapper::toBlogPost)
                .toList();
    }

    public BlogDtos.BlogPostResponse detail(String slug) {
        return blogPostRepository.findBySlug(slug)
                .filter(BlogPostEntity::isPublished)
                .map(apiMapper::toBlogPost)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.blog-post-not-found", "Blog post not found"));
    }

    @Transactional
    public BlogDtos.BlogPostResponse create(String userId, BlogDtos.UpsertBlogPostRequest request) {
        requireAdmin(userId);
        BlogPostEntity post = new BlogPostEntity();
        post.setId(UUID.randomUUID().toString());
        apply(post, request, true);
        return apiMapper.toBlogPost(blogPostRepository.save(post));
    }

    @Transactional
    public BlogDtos.BlogPostResponse update(String userId, String postId, BlogDtos.UpsertBlogPostRequest request) {
        requireAdmin(userId);
        BlogPostEntity post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.blog-post-not-found", "Blog post not found"));
        apply(post, request, false);
        return apiMapper.toBlogPost(blogPostRepository.save(post));
    }

    @Transactional
    public BlogDtos.BlogPostResponse publishPost(String userId, String postId) {
        requireAdmin(userId);
        BlogPostEntity post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.blog-post-not-found", "Blog post not found"));
        post.setPublished(true);
        if (post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDate.now());
        }
        return apiMapper.toBlogPost(blogPostRepository.save(post));
    }

    @Transactional
    public BlogDtos.BlogPostResponse unpublishPost(String userId, String postId) {
        requireAdmin(userId);
        BlogPostEntity post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.blog-post-not-found", "Blog post not found"));
        post.setPublished(false);
        return apiMapper.toBlogPost(blogPostRepository.save(post));
    }

    @Transactional
    public void delete(String userId, String postId) {
        requireAdmin(userId);
        BlogPostEntity post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.blog-post-not-found", "Blog post not found"));
        blogPostRepository.delete(post);
    }

    private void apply(BlogPostEntity post, BlogDtos.UpsertBlogPostRequest request, boolean create) {
        validate(request);
        if (create || !StringUtils.hasText(post.getSlug())) {
            post.setSlug(uniqueSlug(slugify(request.titleEs())));
        }
        post.setTitleEs(request.titleEs().trim());
        post.setTitleEn(request.titleEn().trim());
        post.setTitleCa(StringUtils.hasText(request.titleCa()) ? request.titleCa().trim() : request.titleEs().trim());
        post.setExcerptEs(request.excerptEs().trim());
        post.setExcerptEn(request.excerptEn().trim());
        post.setExcerptCa(StringUtils.hasText(request.excerptCa()) ? request.excerptCa().trim() : request.excerptEs().trim());
        post.setContentEs(request.contentEs().trim());
        post.setContentEn(request.contentEn().trim());
        post.setContentCa(StringUtils.hasText(request.contentCa()) ? request.contentCa().trim() : request.contentEs().trim());
        post.setCoverImageUrl(request.coverImageUrl());
        post.setFeatured(request.featured());
        post.setPublished(request.published());
        post.setPublishedAt(request.published() ? (post.getPublishedAt() == null ? LocalDate.now() : post.getPublishedAt()) : null);
    }

    private void validate(BlogDtos.UpsertBlogPostRequest request) {
        if (!StringUtils.hasText(request.titleEs())
                || !StringUtils.hasText(request.titleEn())
                || !StringUtils.hasText(request.excerptEs())
                || !StringUtils.hasText(request.excerptEn())
                || !StringUtils.hasText(request.contentEs())
                || !StringUtils.hasText(request.contentEn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
        }
    }

    private void requireAdmin(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found", "User not found"));
        if (user.getRole() != UserRole.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.blog-admin-required", "Administrator access required");
        }
    }

    private String uniqueSlug(String base) {
        if (!blogPostRepository.existsBySlug(base)) {
            return base;
        }
        int suffix = 2;
        while (blogPostRepository.existsBySlug(base + "-" + suffix)) {
            suffix++;
        }
        return base + "-" + suffix;
    }

    private String slugify(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
