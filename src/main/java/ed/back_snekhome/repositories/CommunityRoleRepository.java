package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CommunityRoleRepository extends JpaRepository<CommunityRole, Long> {
    Optional<CommunityRole> findTopByCommunityAndIsCreator(Community community, boolean isCreator);
    List<CommunityRole> findAllByCommunity(Community community);
    boolean existsByCommunityAndTitle(Community community, String title);
    Optional<CommunityRole> findByCommunityAndTitle(Community community, String title);
}
