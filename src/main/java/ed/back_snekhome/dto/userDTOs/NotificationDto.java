package ed.back_snekhome.dto.userDTOs;

import ed.back_snekhome.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {

    private boolean isRead;
    private NotificationType type;
    private LocalDateTime date;
    private String message;


    private String groupname;
    private String nickname;
    private Long postId;

}
