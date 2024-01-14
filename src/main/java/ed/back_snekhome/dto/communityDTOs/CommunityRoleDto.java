package ed.back_snekhome.dto.communityDTOs;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class CommunityRoleDto {

    @Size(max = 12, message = "The length of title should not exceed 12 symbols")
    private String title;

    private String bannerColor;
    private String textColor;
    private boolean editId;
    private boolean editDescription;
    private boolean deletePosts;
    private boolean banUser;
    private boolean banCitizen;
    private boolean inviteUsers;
}
