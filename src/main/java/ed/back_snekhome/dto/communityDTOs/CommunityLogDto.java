package ed.back_snekhome.dto.communityDTOs;

import ed.back_snekhome.enums.LogType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommunityLogDto {

    private LogType logType;
    private String actionNickname;
    private String secondNickname;
    private String message;
    private LocalDateTime date;

}
