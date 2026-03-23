package com.karma.platform.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public String id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return authentication.getPrincipal().toString();
    }
}
