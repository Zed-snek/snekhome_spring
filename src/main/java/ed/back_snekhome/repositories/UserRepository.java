package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);



    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByNickname(String nickname);

    Page<UserEntity> findAll(Pageable pageable);

}
