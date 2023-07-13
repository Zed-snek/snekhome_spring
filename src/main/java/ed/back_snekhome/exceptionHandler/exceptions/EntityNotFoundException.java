package ed.back_snekhome.exceptionHandler.exceptions;

import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@NoArgsConstructor
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

}