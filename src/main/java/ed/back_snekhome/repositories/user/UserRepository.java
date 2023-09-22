package ed.back_snekhome.repositories.user;

import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByNicknameIgnoreCase(String nickname);
    boolean existsByEmailIgnoreCase(String email);

    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByNicknameIgnoreCase(String nickname);


    @Query("SELECT u FROM UserEntity u WHERE " +
            "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :r, '%')) " +
            "OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :r, '%')) " +
            "OR LOWER(u.name) LIKE LOWER(CONCAT('%', :r, '%'))")
    List<UserEntity> searchUsersByRequest(
            @Param("r") String request,
            Pageable pageable
    );


}
