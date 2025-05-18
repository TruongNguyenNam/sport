package com.example.storesports.infrastructure.exceptions;

public class DuplicateEntityException extends RuntimeException{
    public DuplicateEntityException(String message) {
    super(message);
    }

}
