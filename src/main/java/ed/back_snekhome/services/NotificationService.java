package ed.back_snekhome.services;


import ed.back_snekhome.entities.user.Notification;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.NotificationType;
import ed.back_snekhome.helperComponents.UserHelper;
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

    /*USER_SUBSCRIBE,
    JOIN_INVITE,
    NEW_COMMENT,
    UPVOTES,
    ELECTIONS_STARTED,
    ELECTIONS_ENDED
     */

    private void createAndSendToUser(Notification.NotificationBuilder builder) {
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

    public void createUserSubscribeNotification(UserEntity onUser) {
        createAndSendToUser(
                builder(onUser, NotificationType.USER_SUBSCRIBE)
                        .secondUser(userHelper.getCurrentUser())
        );
    }







}
