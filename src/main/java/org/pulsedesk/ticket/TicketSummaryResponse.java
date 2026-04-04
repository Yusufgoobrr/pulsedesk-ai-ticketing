package org.pulsedesk.ticket;

public record TicketSummaryResponse(Long id, String title, TicketCategory category, TicketPriority priority,
                                    String summary) {
}
