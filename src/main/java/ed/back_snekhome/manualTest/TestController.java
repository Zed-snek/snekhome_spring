package ed.back_snekhome.manualTest;

import ed.back_snekhome.dto.userDTOs.NotificationDto;
import ed.back_snekhome.enums.NotificationType;
import ed.back_snekhome.response.OwnSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestRepository testRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/api/test")
    public ResponseEntity<OwnSuccessResponse> apiTest() {

        simpMessagingTemplate.convertAndSendToUser("Bwahsteins_Monster", "/receive-notification",
                NotificationDto.builder()
                        .type(NotificationType.BANNED_IN_COMMUNITY)
                        .groupname("scott-pilgrim")
                        .build()
        );

        var response = new OwnSuccessResponse("it worked: ");
        return ResponseEntity.ok(response);
    }

}
