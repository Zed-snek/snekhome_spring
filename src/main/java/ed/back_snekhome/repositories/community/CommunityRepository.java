package ed.back_snekhome.repositories.community;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    boolean existsByGroupnameIgnoreCase(String groupname);
    Optional<Community> findByGroupnameIgnoreCase(String groupname);

    @Query("SELECT c FROM Community c, Membership m WHERE m.user = :user " +
            "AND m.isBanned = false AND m.community = c AND c.isClosed = true")
    List<Community> getClosedCommunitiesByUser(@Param("user") UserEntity user);

    @Query("SELECT c FROM Community c WHERE " +
            "LOWER(c.groupname) LIKE LOWER(CONCAT('%', :r, '%'))" +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :r, '%')) ")
    List<Community> searchCommunitiesByRequest(
            @Param("r") String request,
            Pageable pageable
    );

}
