package com.example.storesports.infrastructure.exceptions;

public class ErrorException extends RuntimeException{
    public ErrorException(String errorCode) {
        super(errorCode);
    }


}
