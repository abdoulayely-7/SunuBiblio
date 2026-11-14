package lydevtech.sunubiblio.notifications.internal.domain;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository {

    void ajouter(Notification notification);

    List<Notification> parAdherent(UUID adherentId);
}
