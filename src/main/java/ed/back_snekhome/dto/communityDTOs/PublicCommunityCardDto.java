package ed.back_snekhome.dto.communityDTOs;

import ed.back_snekhome.enums.CommunityType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PublicCommunityCardDto {

    private String groupname;
    private String image;
    private String description;
    private CommunityType type;
    private int members;

    private String name;


}
