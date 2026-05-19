package com.karma.platform.service;

import com.karma.platform.model.UserRole;
import com.karma.platform.persistence.entity.BlogPostEntity;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.repository.BlogPostRepository;
import com.karma.platform.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogServicePublishTest {

    @Mock
    private BlogPostRepository blogPostRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApiMapper apiMapper;

    @InjectMocks
    private BlogService blogService;

    @Test
    void publishPostSetsPublishedFlagAndTimestamp() {
        UserEntity admin = adminUser();
        BlogPostEntity post = draftPost();

        when(userRepository.findById("admin-1")).thenReturn(Optional.of(admin));
        when(blogPostRepository.findById("post-1")).thenReturn(Optional.of(post));
        when(blogPostRepository.save(post)).thenReturn(post);

        blogService.publishPost("admin-1", "post-1");

        assertTrue(post.isPublished());
        assertTrue(post.getPublishedAt() != null);
        verify(blogPostRepository).save(post);
    }

    @Test
    void unpublishPostKeepsPublishedAt() {
        UserEntity admin = adminUser();
        BlogPostEntity post = draftPost();
        post.setPublished(true);
        post.setPublishedAt(LocalDate.of(2026, 1, 1));

        when(userRepository.findById("admin-1")).thenReturn(Optional.of(admin));
        when(blogPostRepository.findById("post-1")).thenReturn(Optional.of(post));
        when(blogPostRepository.save(post)).thenReturn(post);

        blogService.unpublishPost("admin-1", "post-1");

        assertFalse(post.isPublished());
        assertEquals(LocalDate.of(2026, 1, 1), post.getPublishedAt());
    }

    private static UserEntity adminUser() {
        UserEntity user = new UserEntity();
        user.setId("admin-1");
        user.setRole(UserRole.ADMIN);
        return user;
    }

    private static BlogPostEntity draftPost() {
        BlogPostEntity post = new BlogPostEntity();
        post.setId("post-1");
        post.setTitleEs("Titulo");
        post.setTitleEn("Title");
        post.setSlug("titulo");
        post.setExcerptEs("Excerpt");
        post.setExcerptEn("Excerpt");
        post.setContentEs("Content");
        post.setContentEn("Content");
        post.setPublished(false);
        return post;
    }

}
