package com.supportflow.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User not found");
    }

     public UserNotFoundException(Long id) {
         super("User with id " + id + " not found");
     }

     public UserNotFoundException(String email) {
        super("User with email " + email + " not found");
     }
}
