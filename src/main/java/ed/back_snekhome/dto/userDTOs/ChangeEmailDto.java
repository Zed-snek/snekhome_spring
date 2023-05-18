package ed.back_snekhome.dto.userDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChangeEmailDto {

    private String email;
    private int zero; //made due to an error occurs trying to send DTO with only one variable

}
