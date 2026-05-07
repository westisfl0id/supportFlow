package com.supportflow.comment.controller;

import com.supportflow.comment.dto.CommentResponse;
import com.supportflow.comment.dto.CreateCommentRequest;
import com.supportflow.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public CommentResponse create(@RequestBody CreateCommentRequest request) {
        return commentService.create(request);
    }

}
