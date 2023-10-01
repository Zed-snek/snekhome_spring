package ed.back_snekhome.entities.communityDemocracy;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
    @Size(max = 1024, message = "Program must not exceed 1024 symbols")
    private String program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_community")
    private Community community;

    private boolean isActive;

}
