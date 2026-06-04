package com.e_bank.transaction_api.api;

import com.e_bank.transaction_api.fx.ExchangeRateUnavailableException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps failures to RFC-7807 problem responses and logs them, so a production error always has a
 * matching, traceable log line. Extends ResponseEntityExceptionHandler so Spring's own 4xx handling
 * (type mismatch, missing params, validation) is preserved; the catch-all only sees genuinely
 * unexpected exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    ProblemDetail handleBadRequest(Exception ex) {
        log.debug("Bad request: {}", ex.getMessage());          // client error: expected, low signal
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ExchangeRateUnavailableException.class)
    ProblemDetail handleFxUnavailable(ExchangeRateUnavailableException ex) {
        log.error("FX rate unavailable, failing closed", ex);   // server-side: log with cause
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error handling request", ex);     // never leak the stack to the client
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }
}
