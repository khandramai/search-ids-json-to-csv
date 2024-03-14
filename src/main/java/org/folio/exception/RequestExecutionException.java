package org.folio.exception;

public class RequestExecutionException extends RuntimeException {

    public RequestExecutionException(String message) {
        super(message);
    }

    public RequestExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
