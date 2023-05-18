package ed.back_snekhome.dto.userDTOs;

import ed.back_snekhome.entities.InfoTag;
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

}
