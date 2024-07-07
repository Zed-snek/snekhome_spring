package ed.back_snekhome.dto.communityDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class NewCandidateDto {

    private String groupname;
    private String program;

}
