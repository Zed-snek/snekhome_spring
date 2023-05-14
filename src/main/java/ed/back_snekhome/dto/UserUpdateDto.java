package ed.back_snekhome.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class UserUpdateDto {

    private String email;
    private String name;
    private String surname;
    private String nickname;
    private String nicknameColor;

}
