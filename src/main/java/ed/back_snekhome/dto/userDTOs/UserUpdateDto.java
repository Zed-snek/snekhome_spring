package ed.back_snekhome.dto.userDTOs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@ToString
public class UserUpdateDto {

    private String name;
    private String surname;
    private String nicknameColor;
    private String nickname;


}
