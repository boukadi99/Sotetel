package pi.stagepfesotetel.repositories;

import pi.stagepfesotetel.entities.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByResource(String resource);
    List<Incident> findByType(String type);
    List<Incident> findByRecommendationContaining(String keyword);
    List<Incident> findByStatus(String status);

    Page<Incident> findAll(Pageable pageable);
}