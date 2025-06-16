package com.seven.auth.exception;

public class ConflictException extends AuthorizationException {
    public ConflictException(String message) {
        super(message);
    }
}
