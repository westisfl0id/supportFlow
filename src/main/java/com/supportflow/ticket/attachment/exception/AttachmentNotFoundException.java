package com.supportflow.ticket.attachment.exception;

public class AttachmentNotFoundException extends RuntimeException {
    public AttachmentNotFoundException(Long id) {
        super("Вложение с id " + id + " не найдено");
    }
}
