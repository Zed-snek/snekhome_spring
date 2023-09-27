package ed.back_snekhome.dto.userDTOs;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChangePasswordDto {

    @NotBlank(message = "Old password can't be blank")
    private String oldPass;

    @NotBlank(message = "Password must not be blank")
    @Max(value = 256, message = "Password should contain not more than 256 symbols")
    private String newPass;

}
