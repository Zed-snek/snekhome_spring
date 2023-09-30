package ed.back_snekhome.dto.communityDTOs.democracy;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProgressDto {

    private int rating;
    private int needed_rating;
    private int days;
    private int needed_days;

}
