package ed.back_snekhome.exceptionHandler;

import ed.back_snekhome.exceptionHandler.exceptions.*;
import ed.back_snekhome.response.OwnErrorResponse;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;



@ControllerAdvice
@Log4j2
public class MyExceptionHandler {


    @ExceptionHandler(LoginNotFoundException.class)
    public ResponseEntity<OwnErrorResponse> userErrorHandler(LoginNotFoundException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage()
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); //401
    }



    @ExceptionHandler(PasswordDoesntMatchException.class)
    public ResponseEntity<OwnErrorResponse> userErrorHandler(PasswordDoesntMatchException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage()
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); //401
    }



    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<OwnErrorResponse> userErrorHandler(UnauthorizedException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage()
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); //401
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<OwnErrorResponse> entityNotFoundHandler(EntityNotFoundException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND); //404
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<OwnErrorResponse> userErrorHandler(UserAlreadyExistsException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<OwnErrorResponse> entityErrorHandler(EntityAlreadyExistsException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<OwnErrorResponse> userErrorHandler(TokenExpiredException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(SendEmailErrorException.class)
    public ResponseEntity<OwnErrorResponse> emailErrorHandler(SendEmailErrorException e) {

        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error sending email"
        );
        log.error("Error sending email", e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<OwnErrorResponse> badRequestError(BadRequestException e) {
        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        log.error(e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler(FileCantDeleteException.class)
    public ResponseEntity<OwnErrorResponse> badRequestError(FileCantDeleteException e) {
        var errorResponse = new OwnErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error while deleting the file"
        );
        log.error("Error while deleting the file", e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); //400
    }


}
