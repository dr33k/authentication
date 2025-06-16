package com.seven.auth.util.controllers_advice;

import com.seven.auth.exception.*;
import com.seven.auth.util.exception.*;
import com.seven.auth.util.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.seven.auth.util.response.Responder.*;

@ControllerAdvice
public class AuthorizationExceptionHandler {
    @ExceptionHandler(AuthorizationException.class)
    protected ResponseEntity <Response> handleResponseStatusException(AuthorizationException ex) {
        Class<? extends AuthorizationException> exClass = ex.getClass();
        if (exClass.equals(ClientException.class)) {
            return badRequest(ex.getMessage());
        } else if (exClass.equals(NotFoundException.class)) {
            return notFound(ex.getMessage());
        } else if (exClass.equals(ForbiddenException.class)) {
            return forbidden(ex.getMessage());
        } else if (exClass.equals(ConflictException.class)) {
            return conflict(ex.getMessage());
        }
        return internalServerError(ex.getMessage());
    }
}
