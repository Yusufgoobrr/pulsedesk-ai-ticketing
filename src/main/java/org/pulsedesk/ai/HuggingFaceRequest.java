package org.pulsedesk.ai;

import java.util.List;

public record HuggingFaceRequest(
        String model,
        List<Message> messages,
        boolean stream
) {
    public record Message(String role, String content) {
    }
}