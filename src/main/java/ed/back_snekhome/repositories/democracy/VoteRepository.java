package ed.back_snekhome.repositories.democracy;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.communityDemocracy.Vote;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Modifying
    @Query("DELETE FROM Vote v WHERE v.candidate IN (SELECT c FROM Candidate c WHERE c.community = :community)")
    void deleteAllByCommunity(@Param("community") Community community);

    boolean existsByCandidateAndVoter(Candidate candidate, UserEntity voter);

    int countAllByCandidate(Candidate candidate);

}
