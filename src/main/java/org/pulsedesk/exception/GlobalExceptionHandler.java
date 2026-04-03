package org.pulsedesk.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AiAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleAiAnalysisException(
            AiAnalysisException ex,
            HttpServletRequest request
    ) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<ErrorResponse> handleInvalidEnum(HttpMessageNotReadableException ex, HttpServletRequest request) {
//        Throwable cause = ex.getCause();
//        if (cause instanceof JsonParseException) {
//            return ResponseEntity.badRequest()
//                    .body(new ErrorResponse(ex.getMessage(),
//                            HttpStatus.NOT_FOUND.getReasonPhrase(),
//                            HttpStatus.NOT_FOUND.value(),
//                            request.getRequestURI(),
//                            Instant.now()));
//        }
//        if (cause instanceof InvalidFormatException invalidFormat &&
//                invalidFormat.getTargetType() != null &&
//                invalidFormat.getTargetType().isEnum()) {
//            String invalid = invalidFormat.getValue().toString();
//            List<String> valid = Arrays.stream(invalidFormat.getTargetType().getEnumConstants())
//                    .map(Object::toString)
//                    .toList();
//            ErrorResponse error = new ErrorResponse(
//                    ex.getMessage(),
//                    HttpStatus.NOT_FOUND.getReasonPhrase(),
//                    HttpStatus.NOT_FOUND.value(),
//                    request.getRequestURI(),
//                    Instant.now()
//            );
//
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
//        }
//
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }


}