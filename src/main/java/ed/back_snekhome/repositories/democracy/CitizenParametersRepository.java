package ed.back_snekhome.repositories.democracy;

import ed.back_snekhome.entities.communityDemocracy.CommunityCitizenParameters;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CitizenParametersRepository extends JpaRepository<CommunityCitizenParameters, Long> {
}

