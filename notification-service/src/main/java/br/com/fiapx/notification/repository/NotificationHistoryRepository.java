package br.com.fiapx.notification.repository;

import br.com.fiapx.notification.domain.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, UUID> {

    List<NotificationHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
