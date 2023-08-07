package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.community.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    boolean existsByGroupnameIgnoreCase(String groupname);
    Optional<Community> findByGroupnameIgnoreCase(String groupname);

}
