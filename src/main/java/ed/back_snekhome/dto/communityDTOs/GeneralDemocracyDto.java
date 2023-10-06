package ed.back_snekhome.dto.communityDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class GeneralDemocracyDto {

    private int currentUserRating;
    private int currentUserDays;;
    private boolean isCitizenRight;

    private int citizenAmount;
    private int bannedUsersStats;
    private int bannedCitizensStats;
    private int deletedPostsStats;

    private LocalDate electionsDate;
    private boolean isElectionsNow;

    private String currentPresidentProgram;

    private boolean isCurrentUserActiveCandidate;
    private String currentUserProgram;
}
