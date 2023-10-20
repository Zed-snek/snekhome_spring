package ed.back_snekhome.repositories.democracy;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findTopByUserAndCommunity(UserEntity user, Community community);

    @Modifying
    @Query("UPDATE Candidate c SET c.isActive = false WHERE c.community = :community")
    void makeAllCandidatesInactive(@Param("community") Community community);


    List<Candidate> getAllByCommunityAndIsActiveTrue(Community community);


    @Query("SELECT c FROM Candidate c " +
            "JOIN c.votes v " +
            "WHERE c.community = :community " +
            "GROUP BY c ORDER BY COUNT(v) DESC")
    Optional<Candidate> findCandidateWithMostVotes(
            @Param("community") Community community
    );

}
