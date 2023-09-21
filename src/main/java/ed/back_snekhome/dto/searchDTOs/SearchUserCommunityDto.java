package ed.back_snekhome.dto.searchDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class SearchUserCommunityDto {

    List<SearchItemDto> users;
    List<SearchItemDto> communities;
}
