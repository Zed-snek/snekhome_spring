package ed.back_snekhome.repositories.post;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.post.Commentary;
import ed.back_snekhome.entities.post.CommentaryRating;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.RatingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentaryRatingRepository extends JpaRepository<CommentaryRating, Long> {

    int countByCommentaryAndType(Commentary commentary, RatingType type);
    Optional<CommentaryRating> getTopByCommentaryAndUser(Commentary commentary, UserEntity user);

    @Query("SELECT COUNT(r) FROM CommentaryRating r WHERE r.user = :user AND r.commentary.post.community = :community")
    int countAllByCommunityAndUser(@Param("community") Community community, @Param("user") UserEntity user);
}
