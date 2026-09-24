package pi.stagepfesotetel.repositories;

import pi.stagepfesotetel.entities.ONT;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ONTRepository extends JpaRepository<ONT, Long> {

    // ========== SANS PAGINATION ==========
    List<ONT> findByStatus(String status);
    List<ONT> findBySplitterId(Long splitterId);

    @Query("SELECT o FROM ONT o WHERE o.rxPower < -25")
    List<ONT> findCriticalRxPower();

    @Query("SELECT COUNT(o) FROM ONT o WHERE o.status = :status")
    long countByStatus(@Param("status") String status);

    // ========== AVEC PAGINATION ==========
    Page<ONT> findAll(Pageable pageable);

    Page<ONT> findByStatus(String status, Pageable pageable);

    @Query("SELECT o FROM ONT o WHERE o.rxPower < -25")
    Page<ONT> findCriticalRxPower(Pageable pageable);

    // ========== FILTRAGE AVANCÉ - VERSION SIMPLIFIÉE (SANS RECHERCHE TEXTUELLE) ==========
    @Query("SELECT o FROM ONT o WHERE " +
            "(:status IS NULL OR o.status IN :status) AND " +
            "(:minRxPower IS NULL OR o.rxPower >= :minRxPower) AND " +
            "(:maxRxPower IS NULL OR o.rxPower <= :maxRxPower) AND " +
            "(:minTxPower IS NULL OR o.txPower >= :minTxPower) AND " +
            "(:maxTxPower IS NULL OR o.txPower <= :maxTxPower) AND " +
            "(:minDistance IS NULL OR o.distanceKm >= :minDistance) AND " +
            "(:maxDistance IS NULL OR o.distanceKm <= :maxDistance) AND " +
            "(:splitterId IS NULL OR o.splitter.id = :splitterId)")
    Page<ONT> filterOnts(
            @Param("status") List<String> status,
            @Param("minRxPower") Double minRxPower,
            @Param("maxRxPower") Double maxRxPower,
            @Param("minTxPower") Double minTxPower,
            @Param("maxTxPower") Double maxTxPower,
            @Param("minDistance") Double minDistance,
            @Param("maxDistance") Double maxDistance,
            @Param("splitterId") Long splitterId,
            Pageable pageable
    );
}