package com.karma.platform.service.discussion;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SimpleContentSanitizer {

    public String sanitize(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String normalized = content
                .replaceAll("(?is)<script.*?>.*?</script>", "")
                .replaceAll("(?is)<style.*?>.*?</style>", "")
                .replaceAll("<[^>]+>", "")
                .replaceAll("javascript:", "")
                .trim();
        return normalized;
    }
}
