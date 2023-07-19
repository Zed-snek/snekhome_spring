package ed.back_snekhome.services;

import ed.back_snekhome.dto.postDTOs.NewCommentaryDto;
import ed.back_snekhome.entities.post.Commentary;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.repositories.CommentaryRatingRepository;
import ed.back_snekhome.repositories.CommentaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentaryService {

    private final CommentaryRepository commentaryRepository;
    private final CommentaryRatingRepository commentaryRatingRepository;
    private final PostService postService;
    private final UserMethodsService userMethodsService;


    public void newComment(Long id, NewCommentaryDto dto) {
        var comment = Commentary.builder()
                .post(postService.getPostById(id))
                .user(userMethodsService.getCurrentUser())
                .text(dto.getText())
                .referenceId(dto.getReferenceId())
                .build();
        commentaryRepository.save(comment);
    }

    public void rateComment(Long id, RatingType newStatus) {

    }

}
