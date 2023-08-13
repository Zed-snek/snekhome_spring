package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> getByIdPost(Long idPost);
    List<Post> getPostsByUser(UserEntity user);
}
