package ed.back_snekhome.dto.communityDTOs.democracy;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class GeneralDemocracyDto {

    private ProgressDto progressDto;
    private boolean isCitizenRight;

    private int citizenAmount;
    private int bannedUsersStats;
    private int bannedCitizensStats;
    private int deletedPostsStats;

    private LocalDate electionsDate;
    private boolean isElectionsNow;

    private boolean isCurrentUserCandidate;
    private String candidateProgram;
}
