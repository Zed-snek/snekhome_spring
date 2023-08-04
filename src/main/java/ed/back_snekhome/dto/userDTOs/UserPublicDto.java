package ed.back_snekhome.dto.userDTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.user.InfoTag;
import ed.back_snekhome.entities.user.UserImage;
import ed.back_snekhome.enums.FriendshipType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT) /*to ignore uninitialised integers*/
public class UserPublicDto {

    private String name;
    private String surname;
    private String image;

    private List<UserImage> images;

    private String nickname;
    private String nicknameColor;

    private int friends;
    private int communities;

    private List<InfoTag> tags;

    private FriendshipType friendshipType;

    private CommunityRole communityRole;

}
