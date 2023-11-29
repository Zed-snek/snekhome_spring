package ed.back_snekhome.repositories.community;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CommunityRoleRepository extends JpaRepository<CommunityRole, Long> {

    @Query("SELECT r FROM CommunityRole r WHERE r.community = :community AND r.isCreator = true")
    Optional<CommunityRole> findCreatorRoleOfCommunity(@Param("community") Community community);

    @Query("SELECT r FROM CommunityRole r WHERE r.community = :community AND r.isCitizen = true")
    Optional<CommunityRole> findCitizenRoleOfCommunity(@Param("community") Community community);

    List<CommunityRole> findAllByCommunity(Community community);

    boolean existsByCommunityAndTitle(Community community, String title);

    Optional<CommunityRole> findByCommunityAndTitle(Community community, String title);
}
