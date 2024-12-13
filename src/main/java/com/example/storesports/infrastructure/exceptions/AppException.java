package com.example.storesports.infrastructure.exceptions;

public class AppException extends RuntimeException{
    public AppException(String errorCode) {
        super(errorCode);
    }
}
