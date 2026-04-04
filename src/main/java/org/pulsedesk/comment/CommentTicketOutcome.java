package org.pulsedesk.comment;

/**
 * @param ticketId id of the persisted ticket, or null if no ticket was created
 */
public record CommentTicketOutcome(Long ticketId) {
}
