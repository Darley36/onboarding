package co.com.nequi.model.exception;

import co.com.nequi.model.enums.ErrorMessage;

public class ValidationException extends ApplicationException{
    public ValidationException(String message, ErrorMessage errorMessage) {
        super(message, errorMessage);
    }
}
