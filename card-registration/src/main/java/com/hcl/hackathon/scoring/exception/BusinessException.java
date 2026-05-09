package com.hcl.hackathon.scoring.exception;

public class BusinessException
        extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}