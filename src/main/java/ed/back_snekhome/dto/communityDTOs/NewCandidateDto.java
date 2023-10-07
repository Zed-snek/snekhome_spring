package ed.back_snekhome.dto.communityDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewCandidateDto {

    private String groupname;
    private String program;

}
