package ed.back_snekhome.repositories.post;

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

    List<Post> getPostsByUserOrderByIdPostDesc(UserEntity user, Pageable pageable);
    List<Post> getPostsByUserAndIsAnonymousOrderByIdPostDesc(UserEntity user, boolean isAnonymous, Pageable pageable);

    List<Post> getPostsByCommunityOrderByIdPostDesc(Community community, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.community IN :communities ORDER BY p.idPost DESC")
    List<Post> getPostsByCommunitiesOrderByIdPostDesc(
            @Param("communities") List<Community> communities,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p WHERE p.isAnonymous = false AND p.user = :user AND " +
            "(p.community.isClosed = false OR p.community IN :communities) " +
            "ORDER BY p.idPost DESC")
    List<Post> getPostsByNotCurrentUserOrderByIdPostDesc(
            @Param("user") UserEntity user,
            @Param("communities") List<Community> communities,
            Pageable pageable
    );
}
