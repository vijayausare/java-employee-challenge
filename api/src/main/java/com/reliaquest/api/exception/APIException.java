package com.reliaquest.api.exception;

public class APIException extends RuntimeException {
    public int statusCode;

    public APIException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
