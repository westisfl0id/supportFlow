package com.supportflow.user.exception;

public class SelfBlockingNotAllowedException extends RuntimeException{
    public SelfBlockingNotAllowedException() {
        super("Нельзя заблокировать самого себя");
    }
}
