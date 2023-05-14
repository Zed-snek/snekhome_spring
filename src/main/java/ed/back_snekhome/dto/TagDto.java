package ed.back_snekhome.dto;

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
