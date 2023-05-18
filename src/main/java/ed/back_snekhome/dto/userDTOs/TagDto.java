package ed.back_snekhome.dto.userDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TagDto {

    private Long id;
    private String title;
    private String text;

}
