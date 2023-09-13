package ed.back_snekhome.repositories.community;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommunityLogRepository extends JpaRepository<CommunityLog, Long> {

    List<CommunityLog> getCommunityLogsByCommunityOrderByIdDesc(Community community, Pageable pageable);

}
