package ed.back_snekhome.repositories.community;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.entities.community.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByCommunityAndUser(Community community, UserEntity user);

    int countAllByCommunityAndIsBanned(Community community, boolean isBanned);
    int countAllByUserAndIsBanned(UserEntity user, boolean isBanned);


    List<Membership> findTop4ByUserAndIsBanned(UserEntity user, boolean isBanned);
    List<Membership> findAllByUserAndIsBanned(UserEntity user, boolean isBanned);
    List<Membership> findAllByCommunityAndIsBanned(Community community, boolean isBanned);
    List<Membership> findAllByCommunityAndRole(Community community, CommunityRole role);
}
