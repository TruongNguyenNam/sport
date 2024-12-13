package com.example.storesports.Infrastructure.exceptions;

public class AppException extends RuntimeException{
    public AppException(String errorCode) {
        super(errorCode);
    }
}
