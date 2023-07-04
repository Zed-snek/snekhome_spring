package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    Optional<UserImage> findByName(String name);
}
