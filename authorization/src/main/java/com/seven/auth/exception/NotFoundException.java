package com.seven.auth.exception;

public class NotFoundException extends AuthorizationException {
    public NotFoundException(String message) {
        super(message);
    }
}
