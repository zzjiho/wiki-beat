package wiki.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
public enum ErrorCode {

    /**
     * 400 BAD REQUEST
     */
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "E001", "Validation failed"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "E002", "Invalid input value"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "E003", "Invalid type"),

    /**
     * 404 NOT FOUND
     */
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "E101", "Resource not found"),

    /**
     * 405 METHOD NOT ALLOWED
     */
    METHOD_NOT_ALLOWED_ERROR(HttpStatus.METHOD_NOT_ALLOWED, "E201", "Method not allowed"),

    /**
     * 500 INTERNAL SERVER ERROR
     */
    INTERNAL_SERVER_ERROR_CODE(HttpStatus.INTERNAL_SERVER_ERROR, "E500", "Internal server error");


    private final HttpStatusCode statusCode;
    private final String code;
    private final String message;

    ErrorCode(HttpStatusCode statusCode, String code, String message) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
    }
}