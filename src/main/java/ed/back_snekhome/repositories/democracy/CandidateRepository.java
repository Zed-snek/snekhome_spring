package ed.back_snekhome.repositories.democracy;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.communityDemocracy.Elections;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;
import java.util.stream.Stream;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findTopByUserAndCommunity(UserEntity user, Community community);


    @Query("SELECT eP.candidate FROM ElectionsParticipation eP " +
            "WHERE eP.elections = :elections " +
            "AND eP.electionsNumber = eP.elections.electionsNumber")
    Stream<Candidate> getAllCurrentByElections(@Param("elections") Elections elections);


}
