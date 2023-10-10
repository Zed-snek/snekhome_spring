package ed.back_snekhome.dto.communityDTOs;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CandidateDto {


    private String name;
    private String surname;
    private String nickname;
    private String image;
    private String program;

    private int votes;

}
