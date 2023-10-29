package ed.back_snekhome.entities.user;


import ed.back_snekhome.dto.userDTOs.NotificationDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.enums.NotificationType;
import ed.back_snekhome.services.NotificationService;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTag;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_account")
    private UserEntity notifiedUser;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    boolean isRead;

    @CreatedDate
    private LocalDateTime date;

    private String message;

    //might be null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_user_id")
    private UserEntity secondUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;


    public NotificationDto getDto() {
        return NotificationDto.builder()
                .isRead(isRead)
                .type(type)
                .date(date)
                .message(message)
                .nickname(secondUser == null ? null : secondUser.getNickname())
                .groupname(community == null ? null : community.getGroupname())
                .postId(post == null ? 0 : post.getIdPost())
                .build();
    }

}


