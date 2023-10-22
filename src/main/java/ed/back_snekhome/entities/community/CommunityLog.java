package ed.back_snekhome.entities.community;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.LogType;
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
public class CommunityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Enumerated(EnumType.STRING)
    private LogType logType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_user_id")
    @JsonIgnore
    private UserEntity actionUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_user_id")
    @JsonIgnore
    private UserEntity secondUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_community")
    @JsonIgnore
    private Community community;

    private String message;

    @CreatedDate
    private LocalDateTime date;

}
