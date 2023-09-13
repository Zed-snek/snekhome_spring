package ed.back_snekhome.repositories.user;

import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.entities.user.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    Optional<UserImage> findByName(String name);
    Optional<UserImage> findTopByUserOrderByIdImageDesc(UserEntity user);
}
