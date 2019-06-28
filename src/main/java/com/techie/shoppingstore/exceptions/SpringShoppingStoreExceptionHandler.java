package com.techie.shoppingstore.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class SpringShoppingStoreExceptionHandler extends ResponseEntityExceptionHandler {

    public SpringShoppingStoreExceptionHandler() {
        super();
    }

    @Override
    protected final ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(exception, message(HttpStatus.BAD_REQUEST, exception), headers, HttpStatus.BAD_REQUEST, request);
    }

    private ApiResponse message(HttpStatus httpStatus, Exception exception) {
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        return new ApiResponse(httpStatus.value(), message);
    }
}
