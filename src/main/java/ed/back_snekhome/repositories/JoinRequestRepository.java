package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.JoinRequest;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    Optional<JoinRequest> findTopByCommunityAndUser(Community community, UserEntity user);
    boolean existsByCommunityAndUser(Community community, UserEntity user);
}
