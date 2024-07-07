package ed.back_snekhome.dto.communityDTOs;

import ed.back_snekhome.enums.CommunityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class NewCommunityDto {

    private String idName;
    private String name;
    private String description;
    private CommunityType type;

    private boolean anonAllowed;
    private boolean isClosed;
    private boolean inviteUsers; //for all closed communities except of anarchy

    //for democracy only, to make citizen rank
    private String title;
    private String bannerColor;
    private String textColor;
    private int citizenDays;
    private int citizenRating;
    private int electionDays;

}

