package ed.back_snekhome.dto.postDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewCommentaryDto {

    private Long referenceId; //-1 = none

    @NotBlank(message = "Commentary can't be empty")
    @Size(max = 1024, message = "Length of commentary should not exceed 1024 symbols")
    private String text;

}
