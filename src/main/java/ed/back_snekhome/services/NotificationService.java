package ed.back_snekhome.services;

import ed.back_snekhome.dto.userDTOs.NotificationDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.post.Commentary;
import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.user.Notification;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.NotificationType;
import ed.back_snekhome.helperComponents.UserHelper;
import ed.back_snekhome.repositories.democracy.CandidateRepository;
import ed.back_snekhome.repositories.user.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserHelper userHelper;

    private final NotificationRepository notificationRepository;
    private final CandidateRepository candidateRepository;

    private final static List<Integer> upvoteCounts = List.of(2, 5, 10, 25, 50);

    private void saveAndSendToUser(Notification.NotificationBuilder builder) {
        var notification = builder.build();
        notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSendToUser( //sends to user with WebSockets
                notification.getNotifiedUser().getNickname(),
                "/receive-notification",
                notification.createDto()
        );
    }

    private Notification.NotificationBuilder builder(UserEntity notifiedUser, NotificationType type) {
        return Notification.builder()
                .notifiedUser(notifiedUser)
                .type(type)
                .isRead(false);
    }

    public void createAddFriendNotification(UserEntity toUser, UserEntity fromUser) {
        saveAndSendToUser(builder(toUser, NotificationType.ADD_FRIEND)
                .secondUser(fromUser)
        );
    }
    public void createNewCommentNotification(Commentary comment, Commentary repliedComment) {
        var currentCommentator = userHelper.getCurrentUser();

        //sends notification to post creator
        var postCreator = comment.getPost().getUser();
        var repliedCommentaryCreator = Optional.ofNullable(repliedComment).map(Commentary::getUser);
        if (!currentCommentator.isTheSameUserEntity(postCreator) &&
                repliedCommentaryCreator.filter(c -> c.isTheSameUserEntity(postCreator)).isEmpty()
        ) {
            saveAndSendToUser(
                    builder(comment.getPost().getUser(), NotificationType.POST_REPLY)
                            .commentary(comment)
                            .secondUser(currentCommentator)
            );
        }

        //if user replies on his own commentary, no notification will be sent
        if (repliedCommentaryCreator.isPresent() && !currentCommentator.isTheSameUserEntity(repliedCommentaryCreator.get())) {
            saveAndSendToUser(
                    builder(repliedComment.getUser(), NotificationType.COMMENT_REPLY)
                            .commentary(comment)
                            .secondUser(currentCommentator)
            );
        }

    }


    public void createBannedInCommunityNotification(UserEntity bannedUser, Community toCommunity) {
        saveAndSendToUser(
                builder(bannedUser, NotificationType.BANNED_IN_COMMUNITY)
                        .community(toCommunity)
        );
    }

    public <T> void createUpvotesNotification(T obj, int upvotesCount) {
        if (upvoteCounts.contains(upvotesCount)) {
            if (obj instanceof Post)
                createPostUpvotesNotification((Post) obj, upvotesCount);
            else if (obj instanceof Commentary)
                createCommentUpvotesNotification((Commentary) obj, upvotesCount);
            else
                throw new RuntimeException("Method doesn't allow this type of class");
        }
    }

    private void createPostUpvotesNotification(Post post, int upvotesCount) {
        saveAndSendToUser(
                builder(post.getUser(), NotificationType.POST_UPVOTES)
                        .message(String.valueOf(upvotesCount))
                        .post(post)
        );
    }

    private void createCommentUpvotesNotification(Commentary commentary, int upvotesCount) {
        saveAndSendToUser(
                builder(commentary.getUser(), NotificationType.COMMENT_UPVOTES)
                        .message(String.valueOf(upvotesCount))
                        .commentary(commentary)
        );
    }

    public void createElectionsEndedNotification(Community community) { //to all current candidates
        candidateRepository.getAllCurrentByElections(community.getElections())
                .forEach(candidate -> saveAndSendToUser(
                        builder(candidate.getUser(), NotificationType.ELECTIONS_ENDED)
                                .community(community)
                ));
    }

    @Transactional
    public void readNotificationsOfCurrentUser() {
        notificationRepository.readNotificationsOfUser(userHelper.getCurrentUser());
    }

    public List<NotificationDto> getNotificationsWithPagination(int page, int size) {
        var pageable = PageRequest.of(page, size);

        return notificationRepository.getAllByNotifiedUser(userHelper.getCurrentUser(), pageable)
                .stream()
                .map(Notification::createDto)
                .toList();
    }

    public int countUnreadNotifications(UserEntity user) {
        return notificationRepository.countUnreadNotificationsByUser(user);
    }

}
