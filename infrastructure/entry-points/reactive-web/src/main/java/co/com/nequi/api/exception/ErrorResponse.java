package co.com.nequi.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String message;
    private int code;
    private String stackTrace;
    private LocalDateTime timestamp;
    private String path;

    public static ErrorResponse of(String message, int code, String stackTrace, String path) {
        return ErrorResponse.builder()
                .message(message)
                .code(code)
                .stackTrace(stackTrace)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    public static ErrorResponse of(String message, int code, String path) {
        return ErrorResponse.builder()
                .message(message)
                .code(code)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}
