package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
