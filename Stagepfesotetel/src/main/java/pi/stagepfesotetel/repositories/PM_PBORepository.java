package pi.stagepfesotetel.repositories;

import pi.stagepfesotetel.entities.PM_PBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PM_PBORepository extends JpaRepository<PM_PBO, Long> {
    List<PM_PBO> findByType(String type);
    List<PM_PBO> findByLocationContainingIgnoreCase(String location);
}