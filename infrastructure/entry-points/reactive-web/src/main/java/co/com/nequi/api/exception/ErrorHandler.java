package co.com.nequi.api.exception;

import co.com.nequi.model.enums.ErrorMessage;
import co.com.nequi.model.exception.BusinessException;
import co.com.nequi.model.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ErrorHandler {

    public Mono<ServerResponse> handleError(Throwable error, ServerRequest request) {
        return switch (error) {
            case ValidationException validationException -> validationExceptionWithHttpStatus(validationException, request);
            case BusinessException businessException -> handleExceptionWithHttpStatus(businessException, request);
            default -> defaultHandleExceptionWithHttpStatus(error, request);
        };
    }

    private Mono<ServerResponse> handleExceptionWithHttpStatus(BusinessException ex, ServerRequest request) {

        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorMessage().getMessage()+ex.getMessage(),
                ex.getErrorMessage().getHttpStatus(),
                request.path());
        return ServerResponse
                .status(ex.getErrorMessage().getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

    private Mono<ServerResponse> validationExceptionWithHttpStatus(ValidationException ex, ServerRequest request) {

        ErrorResponse errorResponse = ErrorResponse.of(ex.getMessage(), ex.getErrorMessage().getHttpStatus(),
                request.path());
        return ServerResponse
                .status(ex.getErrorMessage().getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

    private Mono<ServerResponse> defaultHandleExceptionWithHttpStatus(Throwable ex,
                                                                      ServerRequest request) {

        ErrorResponse errorResponse = ErrorResponse.of(ex.getMessage(), ErrorMessage.ERROR_INTERNAL.getHttpStatus(), request.path());
        return ServerResponse
                .status(ErrorMessage.ERROR_INTERNAL.getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }
}
