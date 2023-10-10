package ed.back_snekhome.repositories.democracy;

import ed.back_snekhome.entities.communityDemocracy.Elections;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ElectionsRepository extends JpaRepository<Elections, Long> {
}
