package ed.back_snekhome.services;

import ed.back_snekhome.dto.postDTOs.CommentaryDto;
import ed.back_snekhome.dto.postDTOs.NewCommentaryDto;
import ed.back_snekhome.entities.post.Commentary;
import ed.back_snekhome.entities.post.CommentaryRating;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.CommentaryRatingRepository;
import ed.back_snekhome.repositories.CommentaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CommentaryService {

    private final CommentaryRepository commentaryRepository;
    private final CommentaryRatingRepository commentaryRatingRepository;
    private final PostService postService;
    private final UserMethodsService userMethodsService;
    private final CommunityMethodsService communityMethodsService;


    @Transactional
    public Long newComment(Long id, NewCommentaryDto dto) {
        var user = userMethodsService.getCurrentUser();
        var comment = Commentary.builder()
                .post(postService.getPostById(id))
                .user(user)
                .text(dto.getText())
                .referenceId(dto.getReferenceId())
                .build();
        commentaryRepository.save(comment);

        var rating = CommentaryRating.builder()
                .commentary(comment)
                .user(user)
                .type(RatingType.UPVOTE)
                .build();
        commentaryRatingRepository.save(rating);
        return comment.getIdCommentary();
    }

    @Transactional
    public void deleteComment(Long id) {
        var comment = getCommentaryById(id);
        var user = userMethodsService.getCurrentUser();
        var membership =
                communityMethodsService.getOptionalMembershipOfCurrentUser(comment.getPost().getCommunity());
        if (comment.getUser().equals(user) || (membership.isPresent() && membership.get().getRole().isDeletePosts()))
            deleteAllReferencedComments(id);
        else
            throw new UnauthorizedException("No access to delete commentary");
    }

    private void deleteAllReferencedComments(Long id) {
        var comments = commentaryRepository.findAllByReferenceId(id);
        comments.forEach(comment -> deleteAllReferencedComments(comment.getIdCommentary()));
        commentaryRepository.deleteByIdCommentary(id);
    }

    public void rateComment(Long id, RatingType newStatus) {
        var comment = getCommentaryById(id);
        var rating = findCommentaryRatingOrCreate(comment);
        rating.setType(newStatus);
        commentaryRatingRepository.save(rating);
    }

    private CommentaryRating findCommentaryRatingOrCreate(Commentary commentary) {
        var currentUser = userMethodsService.getCurrentUser();
        var rating = commentaryRatingRepository.getTopByCommentaryAndUser(commentary, currentUser);
        if (rating.isEmpty())
            return CommentaryRating.builder()
                    .commentary(commentary)
                    .user(currentUser)
                    .build();
        else
            return rating.get();
    }
    private int countRating(Commentary comment) {
        return commentaryRatingRepository.countByCommentaryAndType(comment, RatingType.UPVOTE)
                - commentaryRatingRepository.countByCommentaryAndType(comment, RatingType.DOWNVOTE);
    }
    private RatingType getRatedType(Commentary comment, UserEntity user) {
        var rating =
                commentaryRatingRepository.getTopByCommentaryAndUser(comment, user);
        if (rating.isPresent())
            return rating.get().getType();
        return RatingType.NONE;
    }

    private Commentary getCommentaryById(Long id) {
        return commentaryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no commentary"));
    }

    public ArrayList<CommentaryDto> getCommentariesByPostId(Long id) {
        var post = postService.getPostById(id);
        var membership
                = communityMethodsService.getOptionalMembershipOfCurrentUser(post.getCommunity());

        boolean isContext = userMethodsService.isContextUser();
        UserEntity user;
        if (isContext)
            user = userMethodsService.getCurrentUser();
        else
            user = null;

        if (communityMethodsService.isAccessToCommunity(post.getCommunity(), membership)) {
            var list = commentaryRepository.findAllByPost(post);
            ArrayList<CommentaryDto> array = new ArrayList<>();
            list.forEach(comment -> array.add(
                    CommentaryDto.builder()
                            .text(comment.getText())
                            .id(comment.getIdCommentary())
                            .reference(comment.getReferenceId())
                            .nickname(comment.getUser().getNickname())
                            .image(userMethodsService.getTopUserImage(comment.getUser()))
                            .rating(countRating(comment))
                            .ratedType(user == null ? RatingType.NONE : getRatedType(comment, user))
                    .build())
            );
            return array;
        }
        throw new UnauthorizedException("No access to commentaries");
    }

}
