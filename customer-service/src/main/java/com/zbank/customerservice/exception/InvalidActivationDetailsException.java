package com.zbank.customerservice.exception;

public class InvalidActivationDetailsException extends RuntimeException {
    public InvalidActivationDetailsException(String message) {
        super(message);
    }
}
