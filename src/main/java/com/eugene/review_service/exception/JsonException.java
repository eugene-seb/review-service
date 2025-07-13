package com.eugene.review_service.exception;

import java.io.Serial;

public class JsonException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2L;

    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
