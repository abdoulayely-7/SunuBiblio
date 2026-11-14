package lydevtech.sunubiblio.notifications;

import java.util.UUID;

public record NotificationDto(UUID adherentId, String message) {
}
