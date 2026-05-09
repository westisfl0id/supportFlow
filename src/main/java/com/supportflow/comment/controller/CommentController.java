package com.supportflow.comment.controller;

import com.supportflow.comment.dto.CommentResponse;
import com.supportflow.comment.dto.CreateCommentRequest;
import com.supportflow.comment.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets/{ticketId}/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(
            @PathVariable @Positive Long ticketId,
            @RequestParam @Positive Long userId,
            @Valid @RequestBody CreateCommentRequest request) {

        return commentService.create(ticketId, userId, request);
    }

}
