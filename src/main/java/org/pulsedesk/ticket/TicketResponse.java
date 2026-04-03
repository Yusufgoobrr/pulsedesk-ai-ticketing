package org.pulsedesk.ticket;

public record TicketResponse(Long id, String title, TicketCategory category, TicketPriority priority, String summary) {
}
