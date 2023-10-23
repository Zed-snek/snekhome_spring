package ed.back_snekhome.repositories.democracy;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.communityDemocracy.ElectionsParticipation;
import ed.back_snekhome.entities.communityDemocracy.Vote;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Modifying
    @Query("DELETE FROM Vote v WHERE v.electionsParticipation.candidate " +
            "IN (SELECT c FROM Candidate c WHERE c.community = :community)")
    void deleteAllByCommunity(@Param("community") Community community);

    boolean existsByElectionsParticipationAndVoter(ElectionsParticipation electionsParticipation, UserEntity voter);

    int countAllByElectionsParticipation(ElectionsParticipation electionsParticipation);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.electionsParticipation.candidate.community = :community")
    int countAllByCommunity(@Param("community") Community community);

    @Query("SELECT v.electionsParticipation.candidate FROM Vote v " +
            "WHERE v.voter = :voter AND v.electionsParticipation.candidate.community = :community")
    Optional<Candidate> getCandidateByCommunityAndVoter(
            @Param("community") Community community,
            @Param("voter") UserEntity voter
    );


}
