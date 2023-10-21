package ed.back_snekhome.dto.communityDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CandidateListDto {

    private int totalVotes;
    private Long votedId;
    private List<CandidateDto> candidates;
}
