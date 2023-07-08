package ed.back_snekhome.dto.postDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostDto {

    private int comments;
    private int rating;

}
