package ed.back_snekhome.exceptionHandler;

import ed.back_snekhome.exceptionHandler.exceptions.*;
import ed.back_snekhome.response.OwnErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;



@ControllerAdvice
public class MyExceptionHandler {


    @ExceptionHandler(LoginNotFoundException.class)
    public ResponseEntity<OwnErrorResponse> userErrorHandler(LoginNotFoundException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); //401
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<OwnErrorResponse> badCredentialsHandler() {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Password is wrong"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); //401
    }

    @ExceptionHandler(PasswordDoesntMatchException.class)
    public ResponseEntity<OwnErrorResponse> userErrorHandler(PasswordDoesntMatchException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage()
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

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<OwnErrorResponse> userErrorHandler(UnauthorizedException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); //401
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<OwnErrorResponse> entityNotFoundHandler(EntityNotFoundException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND); //404
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<OwnErrorResponse> userErrorHandler(UserAlreadyExistsException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<OwnErrorResponse> entityErrorHandler(EntityAlreadyExistsException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<OwnErrorResponse> userErrorHandler(TokenExpiredException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(SendEmailErrorException.class)
    public ResponseEntity<OwnErrorResponse> emailErrorHandler() {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error sending email"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<OwnErrorResponse> badRequestError(BadRequestException e) {
        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


}
