package ed.back_snekhome.repositories.user;

import ed.back_snekhome.entities.user.Notification;
import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop5ByNotifiedUserOrderByIdDesc(UserEntity notifiedUser);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notifiedUser = :user")
    void readNotificationsOfUser(@Param("user") UserEntity notifiedUser);

    List<Notification> getAllByNotifiedUser(UserEntity notifiedUser, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.notifiedUser = :user AND n.isRead = false")
    int countUnreadNotificationsByUser(@Param("user") UserEntity user);

}
