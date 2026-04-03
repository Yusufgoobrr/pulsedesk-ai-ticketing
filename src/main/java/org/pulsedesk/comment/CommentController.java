package org.pulsedesk.comment;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public Long createNewComment(@Valid @RequestBody NewCommentRequest newCommentRequest) {
        return commentService.createNewComment(newCommentRequest);
    }
}
