package ed.back_snekhome.repositories.post;


import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.post.PostRating;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.RatingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRatingRepository extends JpaRepository<PostRating, Long> {

    int countByPostAndType(Post post, RatingType type);
    Optional<PostRating> getTopByPostAndUser(Post post, UserEntity user);
}
