package ed.back_snekhome.services;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;


    /*public void sendToUser() {
        simpMessagingTemplate.convertAndSendToUser();
    }*/

}
