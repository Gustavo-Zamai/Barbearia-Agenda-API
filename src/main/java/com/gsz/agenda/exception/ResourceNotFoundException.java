package com.gsz.agenda.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final HttpStatus status;

    public ResourceNotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.NOT_FOUND;
    }
}