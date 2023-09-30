package ed.back_snekhome.repositories.post;


import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.post.PostRating;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.RatingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostRatingRepository extends JpaRepository<PostRating, Long> {

    int countByPostAndType(Post post, RatingType type);
    Optional<PostRating> getTopByPostAndUser(Post post, UserEntity user);

    @Query("SELECT COUNT(r) FROM PostRating r WHERE r.user = :user AND r.post.community = :community")
    int countAllByCommunityAndUser(Community community, UserEntity user);
}
