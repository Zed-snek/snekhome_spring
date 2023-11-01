package ed.back_snekhome.controllers;


import ed.back_snekhome.dto.userDTOs.NotificationDto;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping("/get_newest")
    public List<NotificationDto> get5Newest() {

        return notificationService.getLast5NotificationsOfCurrentUser();
    }


    @PutMapping("/read_all")
    public ResponseEntity<OwnSuccessResponse> readAll() {
        notificationService.readNotificationsOfCurrentUser();

        var response = new OwnSuccessResponse("Notifications have been read");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/get")
    public List<NotificationDto> getCommunityLogs(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "15", required = false) int size
    ) {

        return notificationService.getNotificationsWithPagination(page, size);
    }


}
