package com.seven.auth.exception;

public class ForbiddenException extends AuthorizationException {
    public ForbiddenException(String message) {
        super(message);
    }
}
