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
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    String program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_elections")
    Elections elections;

}
