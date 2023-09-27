package ed.back_snekhome.dto.userDTOs;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChangeEmailDto {

    @Email(message = "The provided value is not e-mail")
    private String email;

    private int zero; //made due to an error occurs trying to send DTO with only one variable

}
