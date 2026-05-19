package com.karma.platform.service.notification;

public class NonRetryableEmailException extends RuntimeException {

    public NonRetryableEmailException(String message) {
        super(message);
    }
}
