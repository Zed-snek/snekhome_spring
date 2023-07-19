package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.post.Commentary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentaryRepository extends JpaRepository<Commentary, Long> {

}
