package lydevtech.sunubiblio.notifications.internal.application;

import lydevtech.sunubiblio.notifications.NotificationDto;
import lydevtech.sunubiblio.notifications.NotificationsApi;
import lydevtech.sunubiblio.notifications.internal.domain.Notification;
import lydevtech.sunubiblio.notifications.internal.domain.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
class NotificationsService implements NotificationsApi {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationsService.class);

    private final NotificationRepository notifications;

    NotificationsService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    /** Envoi simule : le message est trace dans les logs et garde dans le journal. */
    void prevenir(UUID adherentId, String message) {
        LOG.info("[SMS simulé] adhérent {} : {}", adherentId, message);
        notifications.ajouter(new Notification(adherentId, message));
    }

    @Override
    public List<NotificationDto> messagesDe(UUID adherentId) {
        return notifications.parAdherent(adherentId).stream()
                .map(n -> new NotificationDto(n.adherentId(), n.message()))
                .toList();
    }
}
