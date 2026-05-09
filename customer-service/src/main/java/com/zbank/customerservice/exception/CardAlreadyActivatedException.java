package com.zbank.customerservice.exception;

public class CardAlreadyActivatedException extends RuntimeException {
    public CardAlreadyActivatedException(String message) {
        super(message);
    }
}
