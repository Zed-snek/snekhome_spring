package ed.back_snekhome.dto.postDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewCommentaryDto {

    private Long referenceId; //-1 = none
    private String text;

}
