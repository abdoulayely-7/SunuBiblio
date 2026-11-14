package lydevtech.sunubiblio.notifications.internal.infrastructure.web;

import lydevtech.sunubiblio.notifications.NotificationDto;
import lydevtech.sunubiblio.notifications.NotificationsApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
class NotificationsController {

    private final NotificationsApi notifications;

    NotificationsController(NotificationsApi notifications) {
        this.notifications = notifications;
    }

    @GetMapping("/notifications")
    List<NotificationDto> messagesDe(@RequestParam UUID adherentId) {
        return notifications.messagesDe(adherentId);
    }
}
