package pi.stagepfesotetel.repositories;

import pi.stagepfesotetel.entities.OLT;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OLTRepository extends JpaRepository<OLT, Long> {

    // ========== MÉTHODES EXISTANTES ==========

    List<OLT> findByVendor(String vendor);

    List<OLT> findBySiteContainingIgnoreCase(String site);

    // ========== NOUVELLE MÉTHODE AVEC PAGINATION ==========

    @Query("SELECT o FROM OLT o WHERE " +
            "(:vendor IS NULL OR o.vendor = :vendor) AND " +
            "(:searchSite IS NULL OR LOWER(o.site) LIKE LOWER(CONCAT('%', :searchSite, '%')))")
    Page<OLT> filterOlts(
            @Param("vendor") String vendor,
            @Param("searchSite") String searchSite,
            Pageable pageable
    );
}