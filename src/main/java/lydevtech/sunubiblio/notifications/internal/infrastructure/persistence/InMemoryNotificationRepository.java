package lydevtech.sunubiblio.notifications.internal.infrastructure.persistence;

import lydevtech.sunubiblio.notifications.internal.domain.Notification;
import lydevtech.sunubiblio.notifications.internal.domain.NotificationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** Journal en memoire : les notifications sont de simples messages, pas des donnees a conserver. */
@Repository
class InMemoryNotificationRepository implements NotificationRepository {

    private final Map<UUID, List<Notification>> parAdherent = new ConcurrentHashMap<>();

    @Override
    public void ajouter(Notification notification) {
        parAdherent.computeIfAbsent(notification.adherentId(), id -> new CopyOnWriteArrayList<>())
                .add(notification);
    }

    @Override
    public List<Notification> parAdherent(UUID adherentId) {
        return List.copyOf(parAdherent.getOrDefault(adherentId, List.of()));
    }
}
