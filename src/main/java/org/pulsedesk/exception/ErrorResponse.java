package org.pulsedesk.exception;

import java.time.Instant;

public record ErrorResponse(String message, String error, int statusCode, String path, Instant timestamp) {
}
