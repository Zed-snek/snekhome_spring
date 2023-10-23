package ed.back_snekhome.repositories.democracy;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.communityDemocracy.Elections;
import ed.back_snekhome.entities.communityDemocracy.ElectionsParticipation;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findTopByUserAndCommunity(UserEntity user, Community community);


    @Query("SELECT eP.candidate FROM ElectionsParticipation eP " +
            "WHERE eP.elections = :elections " +
            "AND eP.electionsNumber = eP.elections.electionsNumber")
    List<Candidate> getAllCurrentByElections(@Param("elections") Elections elections);


    @Query("SELECT eP.candidate FROM ElectionsParticipation eP " +
            "WHERE eP.elections = :elections " +
            "AND eP.electionsNumber = eP.elections.electionsNumber - 1")
    List<Candidate> getAllPreviousElectionsCandidates(@Param("elections") Elections elections);


    @Query("SELECT eP.candidate FROM ElectionsParticipation eP " +
            "JOIN eP.votes v " +
            "WHERE eP.candidate.community = :community " +
            "GROUP BY eP.candidate ORDER BY COUNT(v) DESC")
    Optional<Candidate> findCandidateWithMostVotes(
            @Param("community") Community community
    );



}
