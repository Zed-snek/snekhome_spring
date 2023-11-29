package ed.back_snekhome.dto.userDTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResetPasswordDto {

    @NotBlank(message = "Token can't be blank")
    String token;

    @NotBlank(message = "Password can't be blank")
    @Max(value = 256, message = "Password should contain not more than 256 symbols")
    String newPass;

}
