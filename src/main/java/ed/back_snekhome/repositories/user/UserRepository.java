package ed.back_snekhome.repositories.user;

import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByNicknameIgnoreCase(String nickname);
    boolean existsByEmailIgnoreCase(String email);

    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByNicknameIgnoreCase(String nickname);


}
