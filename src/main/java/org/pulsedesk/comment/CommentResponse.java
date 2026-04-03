package org.pulsedesk.comment;

public record CommentResponse(Long id, String comment, CommentSourceChannel sourceChannel) {
}
