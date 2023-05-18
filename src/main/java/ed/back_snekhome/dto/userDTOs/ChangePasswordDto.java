package ed.back_snekhome.dto.userDTOs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChangePasswordDto {

    private String oldPass;
    private String newPass;

}
