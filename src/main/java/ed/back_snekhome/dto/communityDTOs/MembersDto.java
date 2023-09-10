package ed.back_snekhome.dto.communityDTOs;

import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MembersDto {

    private List<String> roles;
    private List<UserPublicDto> users;
    private boolean isContextUserAccess;

}
