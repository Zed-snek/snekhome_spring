package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.post.Commentary;
import ed.back_snekhome.entities.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentaryRepository extends JpaRepository<Commentary, Long> {
    List<Commentary> findAllByPost(Post post);
    void deleteByIdCommentary(Long id);
    Iterable<Commentary> findAllByReferenceId(Long referenceId);
}
