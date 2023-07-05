package ed.back_snekhome.dto.communityDTOs;

import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Builder
public class MembersDto {

    private ArrayList<String> roles;
    private ArrayList<UserPublicDto> users;
    private boolean isContextUserAccess;

}
