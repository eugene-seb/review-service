package com.eugene.review_service.exception;

import java.io.Serial;

public class DuplicatedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DuplicatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
