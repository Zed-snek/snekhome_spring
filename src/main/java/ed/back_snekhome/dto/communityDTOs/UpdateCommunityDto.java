package ed.back_snekhome.dto.communityDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
public class UpdateCommunityDto {

    String oldGroupname; //to identify which community needs the update

    String name;
    String groupname;
    String description;
}
