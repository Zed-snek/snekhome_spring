package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.post.Commentary;
import ed.back_snekhome.entities.post.CommentaryRating;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.RatingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentaryRatingRepository extends JpaRepository<CommentaryRating, Long> {
    int countByCommentaryAndType(Commentary commentary, RatingType type);
    Optional<CommentaryRating> getTopByCommentaryAndUser(Commentary commentary, UserEntity user);
}
