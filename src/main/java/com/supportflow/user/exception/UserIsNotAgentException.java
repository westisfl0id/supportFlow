package com.supportflow.user.exception;

public class UserIsNotAgentException extends RuntimeException {
    public UserIsNotAgentException(Long id) {
        super("User with id " + id + " is not an agent");
    }
}
