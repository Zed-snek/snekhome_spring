package ed.back_snekhome.entities.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.helpful.Rating;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.RatingType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CommentaryRating extends Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Enumerated(EnumType.STRING)
    private RatingType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account")
    @JsonIgnore
    @CreatedBy
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commentary")
    @JsonIgnore
    private Commentary commentary;

}
