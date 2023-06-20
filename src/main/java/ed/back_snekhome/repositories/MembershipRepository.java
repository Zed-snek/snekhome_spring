package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.Community;
import ed.back_snekhome.entities.CommunityRole;
import ed.back_snekhome.entities.UserEntity;
import ed.back_snekhome.entities.relations.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByCommunityAndUser(Community community, UserEntity user);
    int countAllByCommunity(Community community);
    int countAllByUser(UserEntity user);
    Iterable<Membership> findTop4ByUser(UserEntity user);
    Iterable<Membership> findAllByUser(UserEntity user);
    Iterable<Membership> findAllByCommunity(Community community);
    Iterable<Membership> findAllByCommunityAndRole(Community community, CommunityRole role);
}
