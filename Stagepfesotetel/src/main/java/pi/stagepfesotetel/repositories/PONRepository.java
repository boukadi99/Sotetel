package pi.stagepfesotetel.repositories;

import pi.stagepfesotetel.entities.PON;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PONRepository extends JpaRepository<PON, Long> {
    List<PON> findByOltId(Long oltId);
    List<PON> findByTxPowerLessThan(Double threshold);

}