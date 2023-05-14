package ed.back_snekhome.exceptionHandler.exceptions;

public class PasswordDoesntMatchException extends RuntimeException {

    public PasswordDoesntMatchException(String message) {
        super(message);
    }

}
