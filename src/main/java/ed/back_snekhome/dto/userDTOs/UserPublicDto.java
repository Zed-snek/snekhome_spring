package ed.back_snekhome.dto.userDTOs;

import ed.back_snekhome.entities.CommunityRole;
import ed.back_snekhome.entities.InfoTag;
import ed.back_snekhome.enums.FriendshipType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserPublicDto {

    private String name;
    private String surname;
    private String image;

    private String nickname;
    private String nicknameColor;

    private int friends;
    private int communities;

    private List<InfoTag> tags;

    private FriendshipType friendshipType;

    private CommunityRole communityRole;

}
