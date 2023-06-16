package ed.back_snekhome.exceptionHandler.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EntityAlreadyExistsException extends RuntimeException {

    public EntityAlreadyExistsException(String message) {
        super(message);
    }

}
