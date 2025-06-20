package com.aleksgolds.spring.web.wallet.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<AppError> catchResourceNotFoundException(ResourceNotFoundException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(new AppError("RESOURCE NOT FOUND ", e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<FieldsValidationError> catchValidationException(ValidationException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(new FieldsValidationError(e.getErrorFieldsMessages()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<AppError> catchInsufficientFundsException(InsufficientFundsException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(new AppError("NOT ENOUGH FUNDS IN THE WALLET ", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<AppError> catchWalletServiceException(WalletServiceException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(new AppError("WALLET SERVICE ERROR ", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppError> handleAllUncaughtException(Exception e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(new AppError("WALLET SERVICE IS BROKEN ", e.getMessage()),  HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
