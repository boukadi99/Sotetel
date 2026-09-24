package pi.stagepfesotetel.repositories;

import pi.stagepfesotetel.entities.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThresholdRepository extends JpaRepository<Threshold, Long> {
    Optional<Threshold> findByNameAndTechnology(String name, String technology);
    List<Threshold> findByTechnology(String technology);
    List<Threshold> findByNameContaining(String name);
}