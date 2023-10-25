package ed.back_snekhome.repositories.democracy;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.communityDemocracy.Elections;
import ed.back_snekhome.entities.communityDemocracy.ElectionsParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ElectionsParticipationRepository extends JpaRepository<ElectionsParticipation, Long> {

    Optional<ElectionsParticipation> findByElectionsAndCandidate(Elections elections, Candidate candidate);

    @Query("SELECT eP.candidate FROM ElectionsParticipation eP " +
            "WHERE eP.candidate = :candidate " +
            "AND eP.electionsNumber = eP.elections.electionsNumber")
    Optional<ElectionsParticipation> findByCandidateIfActive(@Param("candidate") Candidate candidate);

    @Query("SELECT ep FROM ElectionsParticipation ep " +
            "JOIN ep.votes v " +
            "WHERE ep.candidate.community = :community " +
            "GROUP BY ep " +
            "ORDER BY COUNT(v) DESC")
    Optional<ElectionsParticipation> findParticipantWithMostVotes(
            @Param("community") Community community
    );


    @Query("SELECT eP FROM ElectionsParticipation eP " +
            "WHERE eP.elections = :elections " +
            "AND eP.electionsNumber = eP.elections.electionsNumber - 1")
    List<ElectionsParticipation> getAllPreviousElectionsCandidates(@Param("elections") Elections elections);


    @Modifying
    @Query("UPDATE ElectionsParticipation ep SET ep.numberOfVotes = " +
            "(SELECT COUNT(v) FROM Vote v WHERE v.electionsParticipation = ep) " +
            "WHERE ep.electionsNumber = ep.elections.electionsNumber")
    void updateElectionsParticipationWithVotes(@Param("elections") Elections elections);

}
