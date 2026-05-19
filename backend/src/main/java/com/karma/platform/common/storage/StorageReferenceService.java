package com.karma.platform.common.storage;

import com.karma.platform.persistence.repository.BlogPostRepository;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.GroupPostRepository;
import com.karma.platform.persistence.repository.GroupRepository;
import com.karma.platform.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageReferenceService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final GroupRepository groupRepository;
    private final BlogPostRepository blogPostRepository;
    private final GroupPostRepository groupPostRepository;

    public StorageReferenceService(
            UserRepository userRepository,
            EventRepository eventRepository,
            GroupRepository groupRepository,
            BlogPostRepository blogPostRepository,
            GroupPostRepository groupPostRepository
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.groupRepository = groupRepository;
        this.blogPostRepository = blogPostRepository;
        this.groupPostRepository = groupPostRepository;
    }

    public boolean isKeyReferenced(String key) {
        return userRepository.existsByAvatarUrlContaining(key)
                || eventRepository.existsByCoverImageUrlContaining(key)
                || groupRepository.existsByBannerUrlContaining(key)
                || blogPostRepository.existsByCoverImageUrlContaining(key)
                || groupPostRepository.existsByImageUrlContaining(key);
    }
}
