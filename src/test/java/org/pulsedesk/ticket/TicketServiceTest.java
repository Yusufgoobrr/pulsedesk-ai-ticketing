package org.pulsedesk.ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService underTest;

    @Test
    void getAllTickets() {
        // given
        Ticket ticket = new Ticket("Login issue", TicketCategory.BUG, TicketPriority.HIGH, "User cannot login");
        ticket.setId(1L);

        Pageable pageable = PageRequest.of(0, 1);
        Page<Ticket> page = new PageImpl<>(List.of(ticket));

        when(ticketRepository.findAll(pageable)).thenReturn(page);

        // when
        Page<TicketSummaryResponse> result = underTest.getAllTickets(pageable);

        // then
        assertThat(result.getContent().size()).isEqualTo(1);

        TicketSummaryResponse t = result.getContent().getFirst();
        assertThat(t.id()).isEqualTo(1L);
        assertThat(t.title()).isEqualTo("Login issue");
    }

    @Test
    void getTicketById() {
        // given
        Ticket ticket = new Ticket("Login issue", TicketCategory.ACCOUNT, TicketPriority.HIGH, "User cannot login");
        ticket.setId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        // when
        TicketResponse result = underTest.getTicketById(1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Login issue");
    }
}