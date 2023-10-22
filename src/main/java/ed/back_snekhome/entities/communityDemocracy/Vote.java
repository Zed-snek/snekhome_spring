package ed.back_snekhome.entities.communityDemocracy;

import ed.back_snekhome.entities.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    @CreatedBy
    private UserEntity voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidate")
    private Candidate candidate;

}
