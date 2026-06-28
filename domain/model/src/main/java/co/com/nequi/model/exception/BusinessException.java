package co.com.nequi.model.exception;

import co.com.nequi.model.enums.ErrorMessage;

public class BusinessException extends ApplicationException{

    public BusinessException(String message, ErrorMessage errorMessage) {
        super(message,errorMessage);
    }
}
