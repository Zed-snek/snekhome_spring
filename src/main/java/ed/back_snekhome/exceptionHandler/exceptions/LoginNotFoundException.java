package ed.back_snekhome.exceptionHandler.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LoginNotFoundException extends RuntimeException {

    public LoginNotFoundException(String message) {
        super(message);
    }


}
