package ua.nulp.order_service.exception;

import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {
    private final int status;
    private final ApiErrorResponse response;

    public ExternalServiceException(int status, ApiErrorResponse response) {
        super(response.getMessage());
        this.status = status;
        this.response = response;
    }
}
