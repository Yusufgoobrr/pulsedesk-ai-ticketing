package org.pulsedesk.comment;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Page<CommentResponse> getAllComments(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return commentService.getAllComments(pageable);
    }

    @PostMapping
    public ResponseEntity<Long> createNewComment(
            @Valid @RequestBody NewCommentRequest newCommentRequest
    ) {
        Long id = commentService.createNewComment(newCommentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }
}
