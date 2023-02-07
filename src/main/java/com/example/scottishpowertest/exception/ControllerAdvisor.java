package com.example.scottishpowertest.exception;

import org.apache.logging.slf4j.SLF4JLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ControllerAdvisor {

    private static final Logger log = LoggerFactory.getLogger(SLF4JLogger.class);

    @ExceptionHandler(AccountNotFoundException.class)
    public final ResponseEntity<Object> handleAccountNotFoundException(AccountNotFoundException ex) {

        final String body = ex.getMessage();
        log.error(String.format("Account not found"));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateReadingFoundException.class)
    public final ResponseEntity<Object> handleDuplicateReadingFoundException(DuplicateReadingFoundException ex) {

        final String body = ex.getMessage();
        log.error(String.format("Duplicate reading found"));

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HistoricReadingFoundException.class)
    public final ResponseEntity<Object> handleHistoricReadingFoundException(HistoricReadingFoundException ex) {

        final String body = ex.getMessage();
        log.error(String.format("Historic reading found"));

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ReadingTooLowException.class)
    public final ResponseEntity<Object> handleReadingTooLowException(ReadingTooLowException ex) {

        final String body = ex.getMessage();
        log.error(String.format("Reading value too low"));

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

}
