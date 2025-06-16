package com.seven.auth.util.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude
public class Response<T> {
    private T data;
    private HttpStatus status;
    private Boolean isError;
    private String message;
    private LocalDateTime timestamp;
    private String token;
}