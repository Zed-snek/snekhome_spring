package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.CommunityImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityImageRepository extends JpaRepository<CommunityImage, Long> {
    Optional<CommunityImage> findByName(String name);
}
