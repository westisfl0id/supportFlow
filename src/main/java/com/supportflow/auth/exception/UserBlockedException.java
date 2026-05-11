package com.supportflow.auth.exception;

public class UserBlockedException extends RuntimeException {
    public UserBlockedException() {
        super("User account is blocked");
    }
}
