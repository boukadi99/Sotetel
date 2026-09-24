package pi.stagepfesotetel.repositories;

import pi.stagepfesotetel.entities.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    List<ChatLog> findBySessionId(String sessionId);
    List<ChatLog> findAllByOrderByTimestampDesc();
}