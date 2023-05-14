package ed.back_snekhome.exceptionHandler.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SendEmailErrorException extends RuntimeException {

    public SendEmailErrorException(String message) {
        super(message);
    }

}
