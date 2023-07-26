package ed.back_snekhome.services;

import ed.back_snekhome.dto.postDTOs.CommentaryDto;
import ed.back_snekhome.dto.postDTOs.NewCommentaryDto;
import ed.back_snekhome.entities.post.Commentary;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.CommentaryRatingRepository;
import ed.back_snekhome.repositories.CommentaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CommentaryService {

    private final CommentaryRepository commentaryRepository;
    private final CommentaryRatingRepository commentaryRatingRepository;
    private final PostService postService;
    private final UserMethodsService userMethodsService;
    private final CommunityMethodsService communityMethodsService;


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

    private int countRating(Commentary commentary) {
        return commentaryRatingRepository.countByCommentaryAndType(commentary, RatingType.UPVOTE)
                - commentaryRatingRepository.countByCommentaryAndType(commentary, RatingType.DOWNVOTE);

    }

    public ArrayList<CommentaryDto> getCommentariesByPostId(Long id) {
        var post = postService.getPostById(id);
        var membership
                = communityMethodsService.getOptionalMembershipOfCurrentUser(post.getCommunity());

        if (communityMethodsService.isAccessToCommunity(post.getCommunity(), membership)) {
            var list = commentaryRepository.findAllByPost(post);
            ArrayList<CommentaryDto> array = new ArrayList<>();
            list.forEach(element -> array.add(
                    CommentaryDto.builder()
                            .text(element.getText())
                            .id(element.getIdCommentary())
                            .reference(element.getReferenceId())
                            .nickname(element.getUser().getNickname())
                            .image(userMethodsService.getTopUserImage(element.getUser()))
                            .rating(countRating(element))
                    .build())
            );
            return array;
        }
        throw new UnauthorizedException("No access to commentaries");
    }

}
