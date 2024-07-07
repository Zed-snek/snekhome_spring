package ed.back_snekhome.exceptionHandler;

import ed.back_snekhome.response.OwnErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class HandlerOfExistingExceptions {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<OwnErrorResponse> badCredentialsHandler() {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Password is wrong"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); //401
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<OwnErrorResponse> userDisabledHandle() {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "You have to verify your account on email"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); //401
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<OwnErrorResponse> fileNotFoundHandle() {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Requested file is not found"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<OwnErrorResponse> tokenExpiredHandler() {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Token is expired"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN); //403
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<OwnErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getBindingResult().getAllErrors().get(0).getDefaultMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<OwnErrorResponse> handleValidationExceptions(ConstraintViolationException ex) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getConstraintViolations().stream().toList().get(0).getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

}
