package com.rest.api.advice.exception;

public class CUserExistException extends RuntimeException{
    public CUserExistException() {
    }

    public CUserExistException(String message) {
        super(message);
    }

    public CUserExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
