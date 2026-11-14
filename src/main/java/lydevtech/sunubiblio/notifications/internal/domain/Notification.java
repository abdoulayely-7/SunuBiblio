package lydevtech.sunubiblio.notifications.internal.domain;

import java.util.UUID;

public record Notification(UUID adherentId, String message) {
}
