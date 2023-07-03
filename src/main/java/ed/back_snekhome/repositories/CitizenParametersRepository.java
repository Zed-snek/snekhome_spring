package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.Community;
import ed.back_snekhome.entities.CommunityCitizenParameters;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CitizenParametersRepository extends JpaRepository<CommunityCitizenParameters, Long> {
    Optional<CommunityCitizenParameters> findTopByCommunity(Community community);
}

