package ed.back_snekhome.services;


import ed.back_snekhome.dto.userDTOs.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;


    private void sendToUser(String nickname, NotificationDto dto) {
        simpMessagingTemplate.convertAndSendToUser(nickname, "/receive-notification", dto);
    }



}
