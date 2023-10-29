package ed.back_snekhome.repositories.user;

import ed.back_snekhome.entities.user.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {


}
