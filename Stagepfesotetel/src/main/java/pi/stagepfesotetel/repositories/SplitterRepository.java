package pi.stagepfesotetel.repositories;

import pi.stagepfesotetel.entities.Splitter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SplitterRepository extends JpaRepository<Splitter, Long> {
    List<Splitter> findByParentPonId(Long ponId);
    List<Splitter> findByRatio(Integer ratio);
    List<Splitter> findByLossDbGreaterThan(Double threshold);
}