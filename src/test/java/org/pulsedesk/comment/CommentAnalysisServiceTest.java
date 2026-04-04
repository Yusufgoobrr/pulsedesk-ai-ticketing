package org.pulsedesk.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pulsedesk.ai.ChatCompletionResponse;
import org.pulsedesk.ai.HuggingFaceRequest;
import org.pulsedesk.ai.HuggingFaceService;
import org.pulsedesk.exception.AiAnalysisException;
import org.pulsedesk.ticket.Ticket;
import org.pulsedesk.ticket.TicketCategory;
import org.pulsedesk.ticket.TicketPriority;
import org.pulsedesk.ticket.TicketRepository;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CommentAnalysisServiceTest {

    public static final String SYSTEM_PROMPT = "system prompt";
    public static final String AI_MODEL = "claude-sonnet";
    @Mock
    private HuggingFaceService huggingFaceService;
    @Mock
    private TicketRepository ticketRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CommentAnalysisService commentAnalysisService;

    @Mock
    private Resource systemMessageResource;

    @BeforeEach
    void setUp() throws IOException {
        when(systemMessageResource.getContentAsString(any(java.nio.charset.Charset.class))).thenReturn(SYSTEM_PROMPT);
        commentAnalysisService = new CommentAnalysisService(
                huggingFaceService,
                ticketRepository,
                objectMapper,
                AI_MODEL,
                systemMessageResource
        );
    }

    @Test
    void analyzeAndCreateTicketWhenTicketWorthy() {
        // given
        Comment comment = new Comment("hello", CommentSourceChannel.APP_REVIEW);
        String aiResponseJson = "{ \"shouldCreateTicket\": true, \"title\": \"Login Issue\", \"category\": \"ACCOUNT\", \"priority\": \"HIGH\", \"summary\": \"User is unable to log into their account, login attempts are failing\" }";
        ChatCompletionResponse chatCompletionResponse = new ChatCompletionResponse(
                List.of(new ChatCompletionResponse.Choice(new ChatCompletionResponse.Message("assistant", aiResponseJson)))
        );

        when(huggingFaceService.completion(
                new HuggingFaceRequest(
                        AI_MODEL,
                        List.of(
                                new HuggingFaceRequest.Message("system", SYSTEM_PROMPT),
                                new HuggingFaceRequest.Message("user", "hello")
                        ),
                        false
                )
        )).thenReturn(chatCompletionResponse);

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(77L);
            return t;
        });

        // when
        CommentTicketOutcome outcome = commentAnalysisService.analyzeAndCreateTicket(comment).join();

        // then
        ArgumentCaptor<Ticket> ticketArgumentCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketArgumentCaptor.capture());
        var ticket = ticketArgumentCaptor.getValue();
        assertThat(outcome.ticketId()).isEqualTo(77L);
        assertThat(ticket.getTitle()).isEqualTo("Login Issue");
        assertThat(ticket.getCategory()).isEqualTo(TicketCategory.ACCOUNT);
        assertThat(ticket.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(ticket.getSummary()).isEqualTo("User is unable to log into their account, login attempts are failing");
    }

    @Test
    void shouldNotSaveTheTicketWhenNotATicket() {
        // given
        Comment comment = new Comment("hello", CommentSourceChannel.APP_REVIEW);
        String aiResponseJson = "{ \"shouldCreateTicket\": false, \"title\": \"Happy With Support\", \"category\": \"ACCOUNT\", \"priority\": \"HIGH\", \"summary\": \"User is unable to log into their account, login attempts are failing\" }";

        ChatCompletionResponse chatCompletionResponse = new ChatCompletionResponse(
                List.of(new ChatCompletionResponse.Choice(new ChatCompletionResponse.Message("assistant", aiResponseJson)))
        );

        when(huggingFaceService.completion(
                new HuggingFaceRequest(
                        "claude-sonnet",
                        List.of(
                                new HuggingFaceRequest.Message("system", "system prompt"),
                                new HuggingFaceRequest.Message("user", "hello")
                        ),
                        false
                )
        )).thenReturn(chatCompletionResponse);

        // when
        CommentTicketOutcome outcome = commentAnalysisService.analyzeAndCreateTicket(comment).join();

        // then
        assertThat(outcome.ticketId()).isNull();
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void shouldThrowErrorWhenCantParseJson() {
        // given
        Comment comment = new Comment("hello", CommentSourceChannel.APP_REVIEW);
        String aiResponseNotJson = "not a json";

        ChatCompletionResponse chatCompletionResponse = new ChatCompletionResponse(
                List.of(new ChatCompletionResponse.Choice(new ChatCompletionResponse.Message("assistant", aiResponseNotJson)))
        );

        when(huggingFaceService.completion(
                new HuggingFaceRequest(
                        "claude-sonnet",
                        List.of(
                                new HuggingFaceRequest.Message("system", SYSTEM_PROMPT),
                                new HuggingFaceRequest.Message("user", "hello")
                        ),
                        false
                )
        )).thenReturn(chatCompletionResponse);

        // when
        // then

        assertThatThrownBy(() ->
                commentAnalysisService.analyzeAndCreateTicket(comment)
        ).isInstanceOf(AiAnalysisException.class).hasMessage("Failed to analyze comment with AI: Unrecognized token 'not': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n" +
                " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 4]");
        verify(ticketRepository, never()).save(any());
    }
}
