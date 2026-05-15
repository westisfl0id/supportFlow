package com.supportflow.user.exception;

public class AdminBlockingNotAllowedException extends RuntimeException {
    public AdminBlockingNotAllowedException() {
        super("Нельзя заблокировать администратора");
    }
}
