package com.karma.platform.common;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String messageCode;
    private final Object[] messageArgs;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.messageCode = null;
        this.messageArgs = new Object[0];
    }

    public ApiException(HttpStatus status, String messageCode, String fallbackMessage, Object... messageArgs) {
        super(fallbackMessage);
        this.status = status;
        this.messageCode = messageCode;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }
}
