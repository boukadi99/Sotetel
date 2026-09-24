package pi.stagepfesotetel.services;

import pi.stagepfesotetel.entities.*;
import pi.stagepfesotetel.exceptions.ResourceNotFoundException;
import pi.stagepfesotetel.repositories.*;
import pi.stagepfesotetel.dto.OntFilterDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private OLTRepository oltRepository;

    @Autowired
    private PONRepository ponRepository;

    @Autowired
    private SplitterRepository splitterRepository;

    @Autowired
    private ONTRepository ontRepository;

    @Autowired
    private PM_PBORepository pmPboRepository;

    // ========== OLT METHODS ==========

    public List<OLT> getAllOlts() {
        return oltRepository.findAll();
    }

    public Optional<OLT> getOltById(Long id) {
        return oltRepository.findById(id);
    }

    public OLT createOlt(OLT olt) {
        // Validation basique
        if (olt.getSite() == null || olt.getSite().trim().isEmpty()) {
            throw new IllegalArgumentException("Le site est obligatoire");
        }
        if (olt.getVendor() == null || olt.getVendor().trim().isEmpty()) {
            throw new IllegalArgumentException("Le vendor est obligatoire");
        }
        if (olt.getTotalPorts() == null || olt.getTotalPorts() <= 0) {
            throw new IllegalArgumentException("Le nombre de ports doit être positif");
        }
        return oltRepository.save(olt);
    }

    public OLT updateOlt(Long id, OLT oltDetails) {
        OLT existingOlt = oltRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OLT", "id", id));

        if (oltDetails.getSite() != null) {
            existingOlt.setSite(oltDetails.getSite());
        }
        if (oltDetails.getVendor() != null) {
            existingOlt.setVendor(oltDetails.getVendor());
        }
        if (oltDetails.getTotalPorts() != null && oltDetails.getTotalPorts() > 0) {
            existingOlt.setTotalPorts(oltDetails.getTotalPorts());
        }

        return oltRepository.save(existingOlt);
    }

    public void deleteOlt(Long id) {
        if (!oltRepository.existsById(id)) {
            throw new ResourceNotFoundException("OLT", "id", id);
        }
        oltRepository.deleteById(id);
    }

    // ========== OLT METHODS WITH PAGINATION ==========

    public Page<OLT> getOltsPaginated(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return oltRepository.findAll(pageable);
    }

    public Page<OLT> filterOlts(String vendor, String searchSite, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return oltRepository.filterOlts(vendor, searchSite, pageable);
    }

    // ========== PON METHODS ==========

    public List<PON> getAllPons() {
        return ponRepository.findAll();
    }

    public Optional<PON> getPonById(Long id) {
        return ponRepository.findById(id);
    }

    public List<PON> getPonsByOltId(Long oltId) {
        if (!oltRepository.existsById(oltId)) {
            throw new ResourceNotFoundException("OLT", "id", oltId);
        }
        return ponRepository.findByOltId(oltId);
    }

    public PON createPon(PON pon) {
        // Vérifier que l'OLT existe
        if (pon.getOlt() == null || pon.getOlt().getId() == null) {
            throw new IllegalArgumentException("L'OLT est obligatoire");
        }
        if (!oltRepository.existsById(pon.getOlt().getId())) {
            throw new ResourceNotFoundException("OLT", "id", pon.getOlt().getId());
        }

        // Validation basique
        if (pon.getPortIndex() == null || pon.getPortIndex() <= 0) {
            throw new IllegalArgumentException("L'index du port doit être positif");
        }

        return ponRepository.save(pon);
    }

    public PON updatePon(Long id, PON ponDetails) {
        PON existingPon = ponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PON", "id", id));

        if (ponDetails.getPortIndex() != null && ponDetails.getPortIndex() > 0) {
            existingPon.setPortIndex(ponDetails.getPortIndex());
        }
        if (ponDetails.getTxPower() != null) {
            existingPon.setTxPower(ponDetails.getTxPower());
        }

        // Si l'OLT change
        if (ponDetails.getOlt() != null && ponDetails.getOlt().getId() != null) {
            if (!oltRepository.existsById(ponDetails.getOlt().getId())) {
                throw new ResourceNotFoundException("OLT", "id", ponDetails.getOlt().getId());
            }
            existingPon.setOlt(ponDetails.getOlt());
        }

        return ponRepository.save(existingPon);
    }

    public void deletePon(Long id) {
        if (!ponRepository.existsById(id)) {
            throw new ResourceNotFoundException("PON", "id", id);
        }
        ponRepository.deleteById(id);
    }

    // ========== SPLITTER METHODS ==========

    public List<Splitter> getAllSplitters() {
        return splitterRepository.findAll();
    }

    public List<Splitter> getSplittersByPonId(Long ponId) {
        if (!ponRepository.existsById(ponId)) {
            throw new ResourceNotFoundException("PON", "id", ponId);
        }
        return splitterRepository.findByParentPonId(ponId);
    }

    public Optional<Splitter> getSplitterById(Long id) {
        return splitterRepository.findById(id);
    }

    public Splitter createSplitter(Splitter splitter) {
        // Vérifier que le PON existe
        if (splitter.getParentPon() == null || splitter.getParentPon().getId() == null) {
            throw new IllegalArgumentException("Le PON parent est obligatoire");
        }
        if (!ponRepository.existsById(splitter.getParentPon().getId())) {
            throw new ResourceNotFoundException("PON", "id", splitter.getParentPon().getId());
        }

        // Validation basique
        if (splitter.getRatio() == null || (splitter.getRatio() != 4 && splitter.getRatio() != 8
                && splitter.getRatio() != 16 && splitter.getRatio() != 32)) {
            throw new IllegalArgumentException("Le ratio doit être 4, 8, 16 ou 32");
        }

        return splitterRepository.save(splitter);
    }

    public Splitter updateSplitter(Long id, Splitter splitterDetails) {
        Splitter existingSplitter = splitterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Splitter", "id", id));

        if (splitterDetails.getRatio() != null) {
            existingSplitter.setRatio(splitterDetails.getRatio());
        }
        if (splitterDetails.getLossDb() != null) {
            existingSplitter.setLossDb(splitterDetails.getLossDb());
        }

        // Si le PON parent change
        if (splitterDetails.getParentPon() != null && splitterDetails.getParentPon().getId() != null) {
            if (!ponRepository.existsById(splitterDetails.getParentPon().getId())) {
                throw new ResourceNotFoundException("PON", "id", splitterDetails.getParentPon().getId());
            }
            existingSplitter.setParentPon(splitterDetails.getParentPon());
        }

        return splitterRepository.save(existingSplitter);
    }

    public void deleteSplitter(Long id) {
        if (!splitterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Splitter", "id", id);
        }
        splitterRepository.deleteById(id);
    }

    // ========== ONT METHODS ==========

    public List<ONT> getAllOnts() {
        return ontRepository.findAll();
    }

    public List<ONT> getOntsByStatus(String status) {
        List<String> validStatus = List.of("online", "offline", "degraded");
        if (!validStatus.contains(status.toLowerCase())) {
            throw new IllegalArgumentException("Statut invalide. Utilisez: online, offline, degraded");
        }
        return ontRepository.findByStatus(status.toLowerCase());
    }

    public List<ONT> getOntsBySplitterId(Long splitterId) {
        if (!splitterRepository.existsById(splitterId)) {
            throw new ResourceNotFoundException("Splitter", "id", splitterId);
        }
        return ontRepository.findBySplitterId(splitterId);
    }

    public List<ONT> getCriticalOnts() {
        return ontRepository.findCriticalRxPower();
    }

    public Optional<ONT> getOntById(Long id) {
        return ontRepository.findById(id);
    }

    public ONT createOnt(ONT ont) {
        // Vérifier que le splitter existe
        if (ont.getSplitter() == null || ont.getSplitter().getId() == null) {
            throw new IllegalArgumentException("Le splitter est obligatoire");
        }
        if (!splitterRepository.existsById(ont.getSplitter().getId())) {
            throw new ResourceNotFoundException("Splitter", "id", ont.getSplitter().getId());
        }

        // Validation basique
        if (ont.getSerial() == null || ont.getSerial().trim().isEmpty()) {
            throw new IllegalArgumentException("Le serial est obligatoire");
        }
        if (ont.getRxPower() == null) {
            throw new IllegalArgumentException("La puissance RX est obligatoire");
        }
        if (ont.getTxPower() == null) {
            throw new IllegalArgumentException("La puissance TX est obligatoire");
        }

        return ontRepository.save(ont);
    }

    public ONT updateOnt(Long id, ONT ontDetails) {
        ONT existingOnt = ontRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ONT", "id", id));

        if (ontDetails.getSerial() != null) {
            existingOnt.setSerial(ontDetails.getSerial());
        }
        if (ontDetails.getRxPower() != null) {
            existingOnt.setRxPower(ontDetails.getRxPower());
        }
        if (ontDetails.getTxPower() != null) {
            existingOnt.setTxPower(ontDetails.getTxPower());
        }
        if (ontDetails.getDistanceKm() != null) {
            existingOnt.setDistanceKm(ontDetails.getDistanceKm());
        }
        if (ontDetails.getStatus() != null) {
            existingOnt.setStatus(ontDetails.getStatus());
        }

        // Si le splitter change
        if (ontDetails.getSplitter() != null && ontDetails.getSplitter().getId() != null) {
            if (!splitterRepository.existsById(ontDetails.getSplitter().getId())) {
                throw new ResourceNotFoundException("Splitter", "id", ontDetails.getSplitter().getId());
            }
            existingOnt.setSplitter(ontDetails.getSplitter());
        }

        return ontRepository.save(existingOnt);
    }

    public void deleteOnt(Long id) {
        if (!ontRepository.existsById(id)) {
            throw new ResourceNotFoundException("ONT", "id", id);
        }
        ontRepository.deleteById(id);
    }

    // ========== ONT METHODS WITH PAGINATION ==========

    public Page<ONT> getOntsPaginated(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ontRepository.findAll(pageable);
    }

    public Page<ONT> getOntsByStatusPaginated(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ontRepository.findByStatus(status, pageable);
    }

    public Page<ONT> getCriticalOntsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ontRepository.findCriticalRxPower(pageable);
    }

    // ========== MÉTHODE FILTRAGE CORRIGÉE (SANS searchSerial) ==========
    public Page<ONT> filterOnts(OntFilterDto filter, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // SUPPRIME filter.getSearchSerial() - 9 paramètres seulement
        return ontRepository.filterOnts(
                filter.getStatus(),
                filter.getMinRxPower(),
                filter.getMaxRxPower(),
                filter.getMinTxPower(),
                filter.getMaxTxPower(),
                filter.getMinDistance(),
                filter.getMaxDistance(),
                filter.getSplitterId(),
                // filter.getSearchSerial(),  // ← COMMENTÉ POUR L'INSTANT
                pageable
        );
    }

// ========== PM/PBO METHODS ==========

    public List<PM_PBO> getAllPmPbo() {
        return pmPboRepository.findAll();
    }

    public List<PM_PBO> getPmPboByType(String type) {
        return pmPboRepository.findByType(type);
    }

    public Optional<PM_PBO> getPmPboById(Long id) {
        return pmPboRepository.findById(id);
    }

    public PM_PBO createPmPbo(PM_PBO pmPbo) {
        // Validation basique
        if (pmPbo.getType() == null || pmPbo.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type est obligatoire");
        }
        if (pmPbo.getCapacity() == null || pmPbo.getCapacity() <= 0) {
            throw new IllegalArgumentException("La capacité doit être positive");
        }
        if (pmPbo.getLocation() == null || pmPbo.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("La localisation est obligatoire");
        }

        return pmPboRepository.save(pmPbo);
    }

    public PM_PBO updatePmPbo(Long id, PM_PBO pmPboDetails) {
        PM_PBO existingPmPbo = pmPboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PM_PBO", "id", id));

        if (pmPboDetails.getType() != null) {
            existingPmPbo.setType(pmPboDetails.getType());
        }
        if (pmPboDetails.getCapacity() != null && pmPboDetails.getCapacity() > 0) {
            existingPmPbo.setCapacity(pmPboDetails.getCapacity());
        }
        if (pmPboDetails.getLocation() != null) {
            existingPmPbo.setLocation(pmPboDetails.getLocation());
        }

        return pmPboRepository.save(existingPmPbo);
    }

    public void deletePmPbo(Long id) {
        if (!pmPboRepository.existsById(id)) {
            throw new ResourceNotFoundException("PM_PBO", "id", id);
        }
        pmPboRepository.deleteById(id);
    }
}