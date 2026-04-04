package org.pulsedesk.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pulsedesk.ai.ChatCompletionResponse;
import org.pulsedesk.ai.CommentAnalysisResult;
import org.pulsedesk.ai.HuggingFaceRequest;
import org.pulsedesk.ai.HuggingFaceService;
import org.pulsedesk.exception.AiAnalysisException;
import org.pulsedesk.ticket.Ticket;
import org.pulsedesk.ticket.TicketRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CommentAnalysisService {

    private final HuggingFaceService huggingFaceService;
    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;
    private final String aiModel;

    private final Resource systemMessageResource;

    public CommentAnalysisService(HuggingFaceService huggingFaceService,
                                  TicketRepository ticketRepository,
                                  ObjectMapper objectMapper,
                                  @Value("${ai.model}") String aiModel,
                                  @Value("classpath:/prompts/system-message.txt") Resource systemMessageResource) {
        this.huggingFaceService = huggingFaceService;
        this.ticketRepository = ticketRepository;
        this.objectMapper = objectMapper;
        this.aiModel = aiModel;
        this.systemMessageResource = systemMessageResource;
    }

    @Async
    public CompletableFuture<CommentTicketOutcome> analyzeAndCreateTicket(Comment comment) {
        try {
            String prompt = systemMessageResource.getContentAsString(StandardCharsets.UTF_8);

            HuggingFaceRequest request = new HuggingFaceRequest(
                    aiModel,
                    List.of(
                            new HuggingFaceRequest.Message("system", prompt),
                            new HuggingFaceRequest.Message("user", comment.getComment())
                    ),
                    false
            );

            ChatCompletionResponse response = huggingFaceService.completion(request);
            CommentAnalysisResult result = objectMapper.readValue(response.choices().getFirst().message().content(), CommentAnalysisResult.class);

            if (result.shouldCreateTicket()) {
                Ticket ticket = new Ticket(
                        result.title(),
                        result.category(),
                        result.priority(),
                        result.summary()
                );
                ticketRepository.save(ticket);
                return CompletableFuture.completedFuture(new CommentTicketOutcome(ticket.getId()));
            }

            return CompletableFuture.completedFuture(new CommentTicketOutcome(null));
        } catch (Exception e) {
            throw new AiAnalysisException("Failed to analyze comment with AI: " + e.getLocalizedMessage(), e);
        }
    }
}