package org.pulsedesk.ai;

import org.pulsedesk.ticket.TicketCategory;
import org.pulsedesk.ticket.TicketPriority;

public record CommentAnalysisResult(boolean shouldCreateTicket, String title, TicketCategory category,
                                    TicketPriority priority, String summary) {
}
