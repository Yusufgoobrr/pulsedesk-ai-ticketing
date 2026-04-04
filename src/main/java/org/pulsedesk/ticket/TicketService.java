package org.pulsedesk.ticket;

import org.pulsedesk.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Page<TicketSummaryResponse> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable)
                .map(ticket -> new TicketSummaryResponse(
                        ticket.getId(),
                        ticket.getTitle(),
                        ticket.getCategory(),
                        ticket.getPriority(),
                        ticket.getSummary()
                ));
    }

    public TicketResponse getTicketById(Long id) {
        return ticketRepository.findById(id).map(ticket -> new TicketResponse(ticket.getId(), ticket.getTitle(), ticket.getCategory(), ticket.getPriority(), ticket.getSummary())).orElseThrow(() -> new ResourceNotFoundException("Ticket with id " + id + " not found"));

    }

}
