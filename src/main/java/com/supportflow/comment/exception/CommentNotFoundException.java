package com.supportflow.comment.exception;

public class CommentNotFoundException extends RuntimeException{
    public CommentNotFoundException() {
        super("Comment not found");
    }

    public CommentNotFoundException(Long id) {
        super("Comment with id " + id + " not found");
    }
}
