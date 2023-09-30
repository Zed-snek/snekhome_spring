package ed.back_snekhome.repositories.community;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.communityDemocracy.CommunityCitizenParameters;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CitizenParametersRepository extends JpaRepository<CommunityCitizenParameters, Long> {
    Optional<CommunityCitizenParameters> findTopByCommunity(Community community);
}

