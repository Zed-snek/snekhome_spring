package ed.back_snekhome.dto.postDTOs;

import ed.back_snekhome.enums.RatingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CommentaryDto {

    private Long id;
    private Long reference;
    private String nickname;
    private String image;
    private int rating;
    private RatingType ratedType;
    private String text;
    private LocalDateTime date;

}
