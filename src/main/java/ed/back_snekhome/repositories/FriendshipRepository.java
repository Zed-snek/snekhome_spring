package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.relations.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findFriendshipByIdFirstUserAndIdSecondUser(Long idFirstUser, Long idSecondUser);

    int countAllByIdFirstUserAndIsFirstUserAndIsSecondUser(Long id, boolean first, boolean second);
    int countAllByIdSecondUserAndIsFirstUserAndIsSecondUser(Long id, boolean first, boolean second);
}
