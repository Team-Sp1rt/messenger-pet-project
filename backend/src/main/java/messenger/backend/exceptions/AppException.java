package messenger.backend.exceptions;

import org.springframework.http.HttpStatus;

public abstract class AppException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    protected AppException(String errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected AppException(String errorCode, HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}