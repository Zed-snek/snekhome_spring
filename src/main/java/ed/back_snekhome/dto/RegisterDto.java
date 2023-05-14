package ed.back_snekhome.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterDto {

    private String name;
    private String surname;
    private String nickname;
    private String email;
    private String password;

}
