package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> getByIdPost(Long idPost);
    List<Post> getPostsByUser(UserEntity user);
    List<Post> getPostsByCommunity(Community community);

    @Query("SELECT p FROM Post p WHERE p.community IN :communities")
    List<Post> getPostsByCommunities(
            @Param("communities") List<Community> communities,
            Pageable pageable);
}
