package ed.back_snekhome.repositories.communityDemocracy;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.communityDemocracy.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Modifying
    @Query("DELETE FROM Vote v WHERE v.candidate.community = :community")
    void deleteAllByCommunity(Community community);
}
