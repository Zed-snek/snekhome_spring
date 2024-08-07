package ed.back_snekhome.controllers;


import ed.back_snekhome.dto.userDTOs.NotificationDto;
import ed.back_snekhome.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/get")
    public List<NotificationDto> getNotifications(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "15", required = false) int size,
            @RequestParam(defaultValue = "false", required = false) boolean isRead
    ) {

        if (isRead)
            notificationService.readNotificationsOfCurrentUser();

        return notificationService.getNotificationsWithPagination(page, size);
    }


}
