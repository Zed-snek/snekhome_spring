package ed.back_snekhome.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommunityRoleDto {

    private String title;
    private String bannerColor;
    private String textColor;
    private boolean editId;
    private boolean editDescription;
    private boolean deletePosts;
    private boolean banUser;
    private boolean banCitizen;
}
