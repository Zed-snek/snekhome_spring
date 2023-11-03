package ed.back_snekhome.repositories.democracy;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.communityDemocracy.Elections;
import ed.back_snekhome.entities.communityDemocracy.ElectionsParticipation;
import ed.back_snekhome.entities.communityDemocracy.Vote;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    void deleteAllByElectionsParticipation_Elections(Elections elections);


    boolean existsByElectionsParticipationAndVoter(ElectionsParticipation electionsParticipation, UserEntity voter);

    @Query("SELECT v.electionsParticipation.candidate FROM Vote v " +
            "WHERE v.voter = :voter AND v.electionsParticipation.candidate.community = :community")
    Optional<Candidate> getCandidateByCommunityAndVoter(
            @Param("community") Community community,
            @Param("voter") UserEntity voter
    );


    @Query("SELECT SUM(ep.numberOfVotes) FROM ElectionsParticipation ep " +
            "WHERE ep.elections = :elections AND ep.electionsNumber = ep.elections.electionsNumber - 1")
    int getTotalVotesOfPrevElections(@Param("elections") Elections elections);


}
