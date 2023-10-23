package ed.back_snekhome.repositories.democracy;

import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.communityDemocracy.Elections;
import ed.back_snekhome.entities.communityDemocracy.ElectionsParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ElectionsParticipationRepository extends JpaRepository<ElectionsParticipation, Long> {

    Optional<ElectionsParticipation> findByElectionsAndCandidate(Elections elections, Candidate candidate);

    @Query("SELECT eP.candidate FROM ElectionsParticipation eP " +
            "WHERE eP.candidate = :candidate " +
            "AND eP.electionsNumber = eP.elections.electionsNumber")
    Optional<ElectionsParticipation> findByCandidateIfActive(@Param("candidate") Candidate candidate);

}
