package ed.back_snekhome.repositories;


import ed.back_snekhome.entities.post.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    Optional<PostImage> findByName(String name);
}

