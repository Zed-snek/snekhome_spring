package ed.back_snekhome.services;


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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserHelper userHelper;

    private final NotificationRepository notificationRepository;
    private final CandidateRepository candidateRepository;



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


    public void createUserSubscribeNotification(UserEntity toUser) {
        saveAndSendToUser(
                builder(toUser, NotificationType.USER_SUBSCRIBE)
                        .secondUser(userHelper.getCurrentUser())
        );
    }


    public void createJoinInviteNotification(UserEntity toUser, Community toCommunity) {
        saveAndSendToUser(
                builder(toUser, NotificationType.JOIN_INVITE)
                        .community(toCommunity)
                        .secondUser(userHelper.getCurrentUser())
        );
    }


    public void createNewCommentNotification(Commentary comment) {
        var commentator = userHelper.getCurrentUser();
        if (!commentator.equals(comment.getUser())) { //if user replies on his own commentary, no notification will be sent
            saveAndSendToUser(
                    builder(comment.getUser(), NotificationType.COMMENT_REPLY)
                            .commentary(comment)
                            .secondUser(commentator)
            );
            if (!comment.getUser().equals(comment.getPost().getUser())) { //post creator will not get 2 notifications for 1 replied commentary
                saveAndSendToUser(
                        builder(comment.getPost().getUser(), NotificationType.POST_REPLY)
                                .commentary(comment)
                                .secondUser(commentator)
                );
            }
        }
    }


    public void createBannedInCommunityNotification(UserEntity bannedUser, Community toCommunity) {
        saveAndSendToUser(
                builder(bannedUser, NotificationType.BANNED_IN_COMMUNITY)
                        .community(toCommunity)
        );
    }


    public void createUpvotesNotification(Post post, int upvotesCount) {
        saveAndSendToUser(
                builder(post.getUser(), NotificationType.POST_UPVOTES)
                        .message(String.valueOf(upvotesCount))
                        .post(post)
        );
    }


    public void createUpvotesNotification(Commentary commentary, int upvotesCount) {
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

    //get last 5 notifications
    //get notifications for notification list with pagination
    //read notifications

}
