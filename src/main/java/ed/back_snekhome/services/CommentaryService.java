package ed.back_snekhome.services;

import ed.back_snekhome.dto.postDTOs.CommentaryDto;
import ed.back_snekhome.dto.postDTOs.NewCommentaryDto;
import ed.back_snekhome.entities.post.Commentary;
import ed.back_snekhome.entities.post.CommentaryRating;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.post.CommentaryRatingRepository;
import ed.back_snekhome.repositories.post.CommentaryRepository;
import ed.back_snekhome.helperComponents.CommunityHelper;
import ed.back_snekhome.helperComponents.MembershipHelper;
import ed.back_snekhome.helperComponents.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentaryService {

    private final CommentaryRepository commentaryRepository;
    private final CommentaryRatingRepository commentaryRatingRepository;
    private final PostService postService;
    private final NotificationService notificationService;
    private final UserHelper userHelper;
    private final CommunityHelper communityHelper;
    private final MembershipHelper membershipHelper;


    @Transactional
    public Long newComment(Long id, NewCommentaryDto dto) {
        var comment = Commentary.builder()
                .post(postService.getPostById(id))
                .text(dto.getText())
                .referenceId(dto.getReferenceId())
                .build();
        commentaryRepository.save(comment);

        var rating = CommentaryRating.builder()
                .commentary(comment)
                .type(RatingType.UPVOTE)
                .build();
        commentaryRatingRepository.save(rating);

        notificationService.createNewCommentNotification(
                comment,
                comment.getReferenceId() == -1L
                        ? null
                        : getCommentaryByIdOrThrowErr(comment.getReferenceId())
        );
        return comment.getIdCommentary();
    }


    @Transactional
    public void deleteComment(Long id) {
        var comment = getCommentaryByIdOrThrowErr(id);
        var user = userHelper.getCurrentUser();
        var membership = membershipHelper
                .getOptionalMembershipOfCurrentUser(comment.getPost().getCommunity());
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
        var comment = getCommentaryByIdOrThrowErr(id);
        var rating = findCommentaryRatingOrCreate(comment);
        rating.setType(newStatus);
        commentaryRatingRepository.save(rating);

        notificationService.createUpvotesNotification(comment, countUpvotes(comment));
    }


    private Commentary getCommentaryByIdOrThrowErr(Long id) {
        return commentaryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no commentary"));
    }


    private CommentaryRating findCommentaryRatingOrCreate(Commentary commentary) {
        var currentUser = userHelper.getCurrentUser();

        return commentaryRatingRepository
                .getTopByCommentaryAndUser(commentary, currentUser)
                .orElse(CommentaryRating.builder()
                        .commentary(commentary)
                        .build()
                );
    }


    private int countUpvotes(Commentary comment) {
        return commentaryRatingRepository.countByCommentaryAndType(comment, RatingType.UPVOTE);
    }


    private int countRating(Commentary comment) {
        return countUpvotes(comment) - commentaryRatingRepository.countByCommentaryAndType(comment, RatingType.DOWNVOTE);
    }


    private RatingType getRatedType(Commentary comment, UserEntity user) {
        var rating =
                commentaryRatingRepository.getTopByCommentaryAndUser(comment, user);
        if (rating.isPresent())
            return rating.get().getType();
        return RatingType.NONE;
    }


    public List<CommentaryDto> getCommentariesByPostId(Long id) {
        var post = postService.getPostById(id);
        var membership = membershipHelper
                .getOptionalMembershipOfCurrentUser(post.getCommunity());

        boolean isContext = userHelper.isContextUser();
        UserEntity user;
        if (isContext)
            user = userHelper.getCurrentUser();
        else
            user = null;

        communityHelper.throwErrIfNoAccessToCommunity(post.getCommunity(), membership);

        var list = commentaryRepository.findAllByPostOrderByIdCommentaryAsc(post);
        return list.stream().map(comment -> CommentaryDto.builder()
                        .text(comment.getText())
                        .id(comment.getIdCommentary())
                        .reference(comment.getReferenceId())
                        .nickname(comment.getUser().getNickname())
                        .image(userHelper.getTopUserImage(comment.getUser()))
                        .rating(countRating(comment))
                        .ratedType(user == null ? RatingType.NONE : getRatedType(comment, user))
                        .date(comment.getDate())
                        .build())
                        .collect(Collectors.toList());
    }


    public void updateCommentary(String text, Long id) {
        var comment = getCommentaryByIdOrThrowErr(id);
        var currentUser = userHelper.getCurrentUser();
        if (comment.getUser().equals(currentUser)) {
            comment.setText(text);
            commentaryRepository.save(comment);
        }
    }

}
