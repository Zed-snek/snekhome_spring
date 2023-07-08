package ed.back_snekhome.exceptionHandler;

import ed.back_snekhome.response.OwnErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.FileNotFoundException;

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

}
