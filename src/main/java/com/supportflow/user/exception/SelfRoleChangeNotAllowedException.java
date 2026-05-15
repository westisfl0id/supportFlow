package com.supportflow.user.exception;

public class SelfRoleChangeNotAllowedException extends RuntimeException {
    public SelfRoleChangeNotAllowedException() {
        super("Нельзя изменять собственную роль");
    }
}
