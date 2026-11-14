package lydevtech.sunubiblio.notifications;

import java.util.List;
import java.util.UUID;

public interface NotificationsApi {

    List<NotificationDto> messagesDe(UUID adherentId);
}
