package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.Community;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    boolean existsByGroupname(String groupname);
    Community findByGroupname(String groupname);
}
