package ed.back_snekhome.dto.searchDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SearchItemDto {

    private String idName;
    private String title;
    private String image;

}
