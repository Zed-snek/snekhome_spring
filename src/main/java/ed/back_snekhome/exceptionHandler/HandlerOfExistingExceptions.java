package ed.back_snekhome.exceptionHandler;

import ed.back_snekhome.response.OwnErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.io.FileNotFoundException;


@ControllerAdvice
@Log4j2
public class HandlerOfExistingExceptions {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<OwnErrorResponse> badCredentialsHandler(BadCredentialsException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Password is wrong"
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); //401
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<OwnErrorResponse> userDisabledHandle(DisabledException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "You have to verify your account on email"
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); //401
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<OwnErrorResponse> fileNotFoundHandle(FileNotFoundException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Requested file is not found"
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<OwnErrorResponse> tokenExpiredHandler(ExpiredJwtException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Token is expired"
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN); //403
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<OwnErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage()
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<OwnErrorResponse> handleValidationExceptions(ConstraintViolationException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getConstraintViolations().stream().toList().get(0).getMessage()
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

}
