package ed.back_snekhome.entities.user;


import ed.back_snekhome.dto.userDTOs.NotificationDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.post.Commentary;
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
    private Long id;

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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "second_user_id")
    private UserEntity secondUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commentary_id")
    private Commentary commentary;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    private Post post;

    public NotificationDto createDto() {
         var builder = NotificationDto.builder()
                 .isRead(isRead)
                 .type(type)
                 .date(date)
                 .message(message)
                 .nickname(secondUser == null ? null : secondUser.getNickname())
                 .groupname(community == null ? null : community.getGroupname())
                 .postId(post == null ? null : post.getIdPost());

         if (commentary != null) {
             var s = commentary.getText();
             builder
                     .postId(commentary.getPost().getIdPost())
                     .message(s.length() > 100 ? s.substring(0, 100) : s);
         }
         return builder.build();
    }

}


