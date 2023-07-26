package ed.back_snekhome.dto.postDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommentaryDto {

    private Long id;
    private Long reference;

    private String nickname;
    private String image;

    private int rating;

    private String text;


}
