package ed.back_snekhome.dto.communityDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NewCommunityDto {

    private String idName;
    private String name;
    private String description;
    //private CommunityType type;

    private boolean isAnonymous;
    private boolean isClosed;

}
