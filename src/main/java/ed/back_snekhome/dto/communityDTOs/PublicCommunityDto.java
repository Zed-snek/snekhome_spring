package ed.back_snekhome.dto.communityDTOs;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
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

}
