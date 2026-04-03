package org.pulsedesk.ai;

import java.util.List;

public record ChatCompletionResponse(
        List<Choice> choices
) {
    public record Choice(Message message) {
    }

    public record Message(String role, String content) {
    }

    public String content() {
        if (choices == null || choices.isEmpty()) return "";
        return choices.getFirst().message().content();
    }
}