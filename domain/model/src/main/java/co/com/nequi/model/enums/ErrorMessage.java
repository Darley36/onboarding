package co.com.nequi.model.enums;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public enum ErrorMessage {
    ERROR_POSTGRES_DUPLICATE("Error with the DB operations.", 409),
    ERROR_POSTGRES("Error with the DB operations.", 500),
    ERROR_POSTGRES_NOT_FOUND("Resource not found in DB. ", 404),
    ERROR_SQS("Error event sqs",500),
    ERROR_DYNAMO_DB("Error dynamoDB", 500),
    ERROR_MONGO_CONNECTION("Error connecting to the DB.", 503),
    ERROR_MONGO_AUTHENTICATION("DB authentication failed.", 401),
    ERROR_MONGO_TIMEOUT("DB operation timeout.", 504),
    ERROR_MONGO_PERMISSION("Insufficient DB permissions.", 403),
    ERROR_FIELDS("Error with the fields of the request.", 400),
    ERROR_NOT_FOUND("Resource not found.", 404),
    ERROR_NOT_FOUND_EXTERNAL_SERVICE("Resource not found in external service", 404),
    ERROR_INTERNAL("We are experiencing issues; please contact the administrator.", 500),
    ERROR_UNAUTHORIZED_EXTERNAL_SERVICE("Error en servicio externo.\n ",502);

    private final String message;
    private final int httpStatus;

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

}
