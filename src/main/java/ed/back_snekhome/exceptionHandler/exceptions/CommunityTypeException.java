package ed.back_snekhome.exceptionHandler.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CommunityTypeException extends RuntimeException {

    public CommunityTypeException(String message) {
        super(message);
    }

}
