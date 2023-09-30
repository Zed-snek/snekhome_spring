package ed.back_snekhome.dto.communityDTOs.democracy;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GeneralDemocracyDto {

    private ProgressDto progressDto;
    private boolean isCitizenRight;
}
