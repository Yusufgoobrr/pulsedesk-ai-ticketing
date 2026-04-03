package org.pulsedesk.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NewCommentRequest(

        @NotBlank String comment, @NotNull CommentSourceChannel sourceChannel) {
}
