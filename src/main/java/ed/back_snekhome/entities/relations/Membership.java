package ed.back_snekhome.entities.relations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.Community;
import ed.back_snekhome.entities.CommunityRole;
import ed.back_snekhome.entities.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "memberships")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_community")
    private Community community;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role")
    private CommunityRole role;

    private boolean isBanned;

}
