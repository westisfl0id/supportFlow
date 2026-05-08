package com.supportflow.exception;

public class ForbiddenActionException extends RuntimeException{
    public ForbiddenActionException() {
        super("Action is forbidden");
    }

    public ForbiddenActionException(String message) {
        super(message);
    }
}
