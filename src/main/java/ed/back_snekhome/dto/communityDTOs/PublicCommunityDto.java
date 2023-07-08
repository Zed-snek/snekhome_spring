package ed.back_snekhome.dto.communityDTOs;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.enums.CommunityType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class PublicCommunityDto {

    private Community community;
    private boolean isMember;
    private boolean isBanned;
    private int members;

    private String ownerNickname;
    private String ownerImage;

    private CommunityRole currentUserRole;

    private boolean isAccess;

    //Limited information
    private String name;
    private String groupname;
    private String description;
    private String image;
    private CommunityType type;

}
