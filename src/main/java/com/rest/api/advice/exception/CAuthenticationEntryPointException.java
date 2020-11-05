package com.rest.api.advice.exception;

public class CAuthenticationEntryPointException extends RuntimeException{
    public CAuthenticationEntryPointException() {

    }

    public CAuthenticationEntryPointException(String message) {
        super(message);
    }

    public CAuthenticationEntryPointException(String message, Throwable cause) {
        super(message, cause);
    }
}
