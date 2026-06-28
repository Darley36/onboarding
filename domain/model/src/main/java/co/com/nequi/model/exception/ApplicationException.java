package co.com.nequi.model.exception;

import co.com.nequi.model.enums.ErrorMessage;

public class ApplicationException extends RuntimeException{

    private final ErrorMessage errorMessage;

    public ApplicationException(String message, ErrorMessage errorMessage) {
        super(message);
        this.errorMessage = errorMessage;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
