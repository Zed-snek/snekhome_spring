package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.Community;
import ed.back_snekhome.entities.CommunityRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CommunityRoleRepository extends JpaRepository<CommunityRole, Long> {
    Optional<CommunityRole> findTopByCommunityAndIsCreator(Community community, boolean isCreator);
    Iterable<CommunityRole> findAllByCommunity(Community community);
}
