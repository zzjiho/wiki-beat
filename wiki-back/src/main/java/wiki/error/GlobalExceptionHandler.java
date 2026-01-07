package wiki.error;


import wiki.api.RestApiResponse;
import wiki.code.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static wiki.code.ErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation binding errors from @Valid or @Validated
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<RestApiResponse<Void>> handleBindException(BindException e) {
        log.error("handleBindException", e);
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RestApiResponse.error(VALIDATION_ERROR, errorMessage));
    }

    /**
     * Handles type mismatch errors, typically when @RequestParam fails to bind to enum
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<RestApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("handleMethodArgumentTypeMismatchException", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RestApiResponse.error(INVALID_TYPE_VALUE, e.getMessage()));
    }

    /**
     * Handles unsupported HTTP method requests
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<RestApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(RestApiResponse.error(METHOD_NOT_ALLOWED_ERROR, e.getMessage()));
    }

    /**
     * Handles all other unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<RestApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RestApiResponse.error(INTERNAL_SERVER_ERROR_CODE, e.getMessage()));
    }

}