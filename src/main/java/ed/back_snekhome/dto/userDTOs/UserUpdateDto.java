package ed.back_snekhome.dto.userDTOs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class UserUpdateDto {

    private String name;
    private String surname;
    private String nicknameColor;
    private String nickname;


}
