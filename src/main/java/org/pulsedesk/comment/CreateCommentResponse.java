package org.pulsedesk.comment;

/**
 * @param ticketId non-null when AI analysis created a support ticket for this comment
 */
public record CreateCommentResponse(Long commentId, Long ticketId) {
}
