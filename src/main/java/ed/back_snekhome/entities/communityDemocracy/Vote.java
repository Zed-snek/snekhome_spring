package ed.back_snekhome.entities.communityDemocracy;

import ed.back_snekhome.entities.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private UserEntity voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidate")
    private Candidate candidate;

}
