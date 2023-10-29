package ed.back_snekhome.manualTest;

import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.CommunityMethodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestRepository testRepository;
    private final CommunityMethodsService communityMethodsService;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/api/test")
    public ResponseEntity<OwnSuccessResponse> apiTest() {

        simpMessagingTemplate.convertAndSendToUser("Bwahsteins_Monster", "/receive-notification", "DAMN WEBSOCKECTS");

        var response = new OwnSuccessResponse("it worked: ");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
