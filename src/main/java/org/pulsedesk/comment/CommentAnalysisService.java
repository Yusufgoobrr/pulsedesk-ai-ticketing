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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CommentAnalysisService {

    private final HuggingFaceService huggingFaceService;
    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;

    @Value("${ai.model}")
    private String aiModel;

    public CommentAnalysisService(HuggingFaceService huggingFaceService,
                                  TicketRepository ticketRepository,
                                  ObjectMapper objectMapper) {
        this.huggingFaceService = huggingFaceService;
        this.ticketRepository = ticketRepository;
        this.objectMapper = objectMapper;
    }

    @Async
    public CompletableFuture<CommentAnalysisResult> analyzeAndCreateTicket(Comment comment) {
        try {
            String prompt = """
                    You are a support triage assistant for PulseDesk, a customer support platform.
                    Your job is to analyze user comments and decide whether they should become support tickets.
                    
                    RULES:
                    - If the comment is a compliment, praise, or general positive feedback with no actionable issue, set "shouldCreateTicket" to false.
                    - If the comment describes a bug, broken feature, billing issue, account problem, or feature request, set "shouldCreateTicket" to true.
                    
                    PRIORITY RULES (only when shouldCreateTicket is true):
                    - HIGH: app is broken, user cannot log in, data loss, payment failed, security concern
                    - MEDIUM: something is not working correctly but a workaround may exist, slow performance, missing data
                    - LOW: feature request, minor UI issue, general suggestion
                    
                    CATEGORY RULES (only when shouldCreateTicket is true):
                    - BUG: something is broken or not working as expected
                    - FEATURE: user is requesting a new feature or improvement
                    - BILLING: anything related to payments, charges, subscriptions, or invoices
                    - ACCOUNT: login issues, password problems, profile or access issues
                    - OTHER: does not fit any of the above
                    
                    INSTRUCTIONS:
                    - Respond ONLY with a valid JSON object.
                    - Do NOT include any explanation, markdown, code blocks, or extra text.
                    - Every field must be present in the response.
                    - If shouldCreateTicket is false, set title, category, priority, and summary to null.
                    
                    Comment: "%s"
                    
                    Respond with exactly this JSON structure:
                    {
                      "shouldCreateTicket": true,
                      "title": "string or null",
                      "category": "BUG | FEATURE | BILLING | ACCOUNT | OTHER | null",
                      "priority": "LOW | MEDIUM | HIGH | null",
                      "summary": "string or null"
                    }
                    """.formatted(comment.getComment());

            HuggingFaceRequest request = new HuggingFaceRequest(
                    aiModel,
                    List.of(new HuggingFaceRequest.Message("user", prompt)),
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
            }

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            throw new AiAnalysisException("Failed to analyze comment with AI", e);
        }
    }
}