package org.pulsedesk.comment;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentAnalysisService commentAnalysisService;

    public CommentService(CommentRepository commentRepository, CommentAnalysisService commentAnalysisService) {
        this.commentRepository = commentRepository;
        this.commentAnalysisService = commentAnalysisService;
    }

    // pagination
    public Page<CommentResponse> getAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable)
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getComment(),
                        comment.getSourceChannel()
                ));
    }

    public CreateCommentResponse createNewComment(@NonNull NewCommentRequest newCommentRequest) {
        Comment comment = new Comment(newCommentRequest.comment(), newCommentRequest.sourceChannel());
        commentRepository.save(comment);
        CommentTicketOutcome outcome = commentAnalysisService.analyzeAndCreateTicket(comment).join();
        return new CreateCommentResponse(comment.getId(), outcome.ticketId());
    }
}
