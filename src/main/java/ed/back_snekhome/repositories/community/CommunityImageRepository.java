package ed.back_snekhome.repositories.community;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityImageRepository extends JpaRepository<CommunityImage, Long> {
    Optional<CommunityImage> findByName(String name);
    Optional<CommunityImage> findTopByCommunityOrderByIdImageDesc(Community community);
}
