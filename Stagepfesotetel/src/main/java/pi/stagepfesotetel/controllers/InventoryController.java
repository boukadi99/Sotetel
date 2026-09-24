package pi.stagepfesotetel.controllers;
import pi.stagepfesotetel.dto.OntFilterDto;
import pi.stagepfesotetel.dto.*;
import pi.stagepfesotetel.entities.*;
import pi.stagepfesotetel.services.InventoryService;
import pi.stagepfesotetel.utils.DtoMapper;
import pi.stagepfesotetel.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import pi.stagepfesotetel.repositories.IncidentRepository;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private DtoMapper dtoMapper;

    // ========== OLT ENDPOINTS ==========

    @GetMapping("/olts")
    public List<OLTDTO> getAllOlts() {
        return dtoMapper.toOltDtoList(inventoryService.getAllOlts());
    }
// ========== OLT ENDPOINTS WITH PAGINATION ==========

    @GetMapping("/olts/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<OLTDTO>> getOltsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        org.springframework.data.domain.Page<OLT> oltPage = inventoryService.getOltsPaginated(page, size, sortBy, direction);
        org.springframework.data.domain.Page<OLTDTO> dtoPage = oltPage.map(dtoMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/olts/filter")
    public ResponseEntity<org.springframework.data.domain.Page<OLTDTO>> filterOlts(
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) String searchSite,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        org.springframework.data.domain.Page<OLT> oltPage = inventoryService.filterOlts(vendor, searchSite, page, size);
        org.springframework.data.domain.Page<OLTDTO> dtoPage = oltPage.map(dtoMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }
    @GetMapping("/olts/{id}")
    public ResponseEntity<OLTDTO> getOltById(@PathVariable Long id) {
        return inventoryService.getOltById(id)
                .map(dtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/olts")
    public ResponseEntity<?> createOlt(@Valid @RequestBody OltCreateDto oltDto) {
        try {
            OLT olt = new OLT();
            olt.setSite(oltDto.getSite());
            olt.setVendor(oltDto.getVendor());
            olt.setTotalPorts(oltDto.getTotalPorts());

            OLT created = inventoryService.createOlt(olt);
            return new ResponseEntity<>(dtoMapper.toDto(created), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/olts/{id}")
    public ResponseEntity<?> updateOlt(@PathVariable Long id, @Valid @RequestBody OltCreateDto oltDto) {
        try {
            OLT olt = new OLT();
            olt.setSite(oltDto.getSite());
            olt.setVendor(oltDto.getVendor());
            olt.setTotalPorts(oltDto.getTotalPorts());

            OLT updated = inventoryService.updateOlt(id, olt);
            return ResponseEntity.ok(dtoMapper.toDto(updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/olts/{id}")
    public ResponseEntity<Void> deleteOlt(@PathVariable Long id) {
        try {
            inventoryService.deleteOlt(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== PON ENDPOINTS ==========

    @GetMapping("/pons")
    public List<PONDTO> getAllPons() {
        return dtoMapper.toPonDtoList(inventoryService.getAllPons());
    }

    @GetMapping("/pons/{id}")
    public ResponseEntity<PONDTO> getPonById(@PathVariable Long id) {
        return inventoryService.getPonById(id)
                .map(dtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pons/olt/{oltId}")
    public ResponseEntity<?> getPonsByOltId(@PathVariable Long oltId) {
        try {
            List<PONDTO> pons = dtoMapper.toPonDtoList(inventoryService.getPonsByOltId(oltId));
            return ResponseEntity.ok(pons);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/pons")
    public ResponseEntity<?> createPon(@Valid @RequestBody PonCreateDto ponDto) {
        try {
            PON pon = new PON();
            pon.setPortIndex(ponDto.getPortIndex());
            pon.setTxPower(ponDto.getTxPower());

            OLT olt = inventoryService.getOltById(ponDto.getOltId())
                    .orElseThrow(() -> new ResourceNotFoundException("OLT", "id", ponDto.getOltId()));
            pon.setOlt(olt);

            PON created = inventoryService.createPon(pon);
            return new ResponseEntity<>(dtoMapper.toDto(created), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/pons/{id}")
    public ResponseEntity<?> updatePon(@PathVariable Long id, @Valid @RequestBody PonCreateDto ponDto) {
        try {
            PON pon = new PON();
            pon.setPortIndex(ponDto.getPortIndex());
            pon.setTxPower(ponDto.getTxPower());

            if (ponDto.getOltId() != null) {
                OLT olt = inventoryService.getOltById(ponDto.getOltId())
                        .orElseThrow(() -> new ResourceNotFoundException("OLT", "id", ponDto.getOltId()));
                pon.setOlt(olt);
            }

            PON updated = inventoryService.updatePon(id, pon);
            return ResponseEntity.ok(dtoMapper.toDto(updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/pons/{id}")
    public ResponseEntity<Void> deletePon(@PathVariable Long id) {
        try {
            inventoryService.deletePon(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== SPLITTER ENDPOINTS ==========

    @GetMapping("/splitters")
    public List<SplitterDTO> getAllSplitters() {
        return dtoMapper.toSplitterDtoList(inventoryService.getAllSplitters());
    }

    @GetMapping("/splitters/{id}")
    public ResponseEntity<SplitterDTO> getSplitterById(@PathVariable Long id) {
        return inventoryService.getSplitterById(id)
                .map(dtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/splitters/pon/{ponId}")
    public ResponseEntity<?> getSplittersByPonId(@PathVariable Long ponId) {
        try {
            List<SplitterDTO> splitters = dtoMapper.toSplitterDtoList(
                    inventoryService.getSplittersByPonId(ponId));
            return ResponseEntity.ok(splitters);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/splitters")
    public ResponseEntity<?> createSplitter(@Valid @RequestBody SplitterCreateDto splitterDto) {
        try {
            Splitter splitter = new Splitter();
            splitter.setRatio(splitterDto.getRatio());
            splitter.setLossDb(splitterDto.getLossDb());

            PON pon = inventoryService.getPonById(splitterDto.getPonId())
                    .orElseThrow(() -> new ResourceNotFoundException("PON", "id", splitterDto.getPonId()));
            splitter.setParentPon(pon);

            Splitter created = inventoryService.createSplitter(splitter);
            return new ResponseEntity<>(dtoMapper.toDto(created), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/splitters/{id}")
    public ResponseEntity<?> updateSplitter(@PathVariable Long id, @Valid @RequestBody SplitterCreateDto splitterDto) {
        try {
            Splitter splitter = new Splitter();
            splitter.setRatio(splitterDto.getRatio());
            splitter.setLossDb(splitterDto.getLossDb());

            if (splitterDto.getPonId() != null) {
                PON pon = inventoryService.getPonById(splitterDto.getPonId())
                        .orElseThrow(() -> new ResourceNotFoundException("PON", "id", splitterDto.getPonId()));
                splitter.setParentPon(pon);
            }

            Splitter updated = inventoryService.updateSplitter(id, splitter);
            return ResponseEntity.ok(dtoMapper.toDto(updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/splitters/{id}")
    public ResponseEntity<Void> deleteSplitter(@PathVariable Long id) {
        try {
            inventoryService.deleteSplitter(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== ONT ENDPOINTS ==========

    @GetMapping("/onts")
    public List<ONTDTO> getAllOnts() {
        return dtoMapper.toOntDtoList(inventoryService.getAllOnts());
    }
// ========== ONT ENDPOINTS WITH PAGINATION ==========

    @GetMapping("/onts/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<ONTDTO>> getOntsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        org.springframework.data.domain.Page<ONT> ontPage = inventoryService.getOntsPaginated(page, size, sortBy, direction);
        org.springframework.data.domain.Page<ONTDTO> dtoPage = ontPage.map(dtoMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/onts/status/{status}/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<ONTDTO>> getOntsByStatusPaginated(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        org.springframework.data.domain.Page<ONT> ontPage = inventoryService.getOntsByStatusPaginated(status, page, size);
        org.springframework.data.domain.Page<ONTDTO> dtoPage = ontPage.map(dtoMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/onts/critical/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<ONTDTO>> getCriticalOntsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        org.springframework.data.domain.Page<ONT> ontPage = inventoryService.getCriticalOntsPaginated(page, size);
        org.springframework.data.domain.Page<ONTDTO> dtoPage = ontPage.map(dtoMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping("/onts/filter")
    public ResponseEntity<org.springframework.data.domain.Page<ONTDTO>> filterOnts(
            @RequestBody OntFilterDto filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        org.springframework.data.domain.Page<ONT> ontPage = inventoryService.filterOnts(filter, page, size, sortBy, direction);
        org.springframework.data.domain.Page<ONTDTO> dtoPage = ontPage.map(dtoMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }
    @GetMapping("/onts/{id}")
    public ResponseEntity<ONTDTO> getOntById(@PathVariable Long id) {
        return inventoryService.getOntById(id)
                .map(dtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/onts/status/{status}")
    public ResponseEntity<?> getOntsByStatus(@PathVariable String status) {
        try {
            List<ONTDTO> onts = dtoMapper.toOntDtoList(inventoryService.getOntsByStatus(status));
            return ResponseEntity.ok(onts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/onts/splitter/{splitterId}")
    public ResponseEntity<?> getOntsBySplitterId(@PathVariable Long splitterId) {
        try {
            List<ONTDTO> onts = dtoMapper.toOntDtoList(
                    inventoryService.getOntsBySplitterId(splitterId));
            return ResponseEntity.ok(onts);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/onts/critical")
    public List<ONTDTO> getCriticalOnts() {
        return dtoMapper.toOntDtoList(inventoryService.getCriticalOnts());
    }

    @PostMapping("/onts")
    public ResponseEntity<?> createOnt(@Valid @RequestBody OntCreateDto ontDto) {
        try {
            ONT ont = new ONT();
            ont.setSerial(ontDto.getSerial());
            ont.setRxPower(ontDto.getRxPower());
            ont.setTxPower(ontDto.getTxPower());
            ont.setDistanceKm(ontDto.getDistanceKm());
            ont.setStatus(ontDto.getStatus());

            Splitter splitter = inventoryService.getSplitterById(ontDto.getSplitterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Splitter", "id", ontDto.getSplitterId()));
            ont.setSplitter(splitter);

            ONT created = inventoryService.createOnt(ont);
            return new ResponseEntity<>(dtoMapper.toDto(created), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/onts/{id}")
    public ResponseEntity<?> updateOnt(@PathVariable Long id, @Valid @RequestBody OntCreateDto ontDto) {
        try {
            ONT ont = new ONT();
            ont.setSerial(ontDto.getSerial());
            ont.setRxPower(ontDto.getRxPower());
            ont.setTxPower(ontDto.getTxPower());
            ont.setDistanceKm(ontDto.getDistanceKm());
            ont.setStatus(ontDto.getStatus());

            if (ontDto.getSplitterId() != null) {
                Splitter splitter = inventoryService.getSplitterById(ontDto.getSplitterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Splitter", "id", ontDto.getSplitterId()));
                ont.setSplitter(splitter);
            }

            ONT updated = inventoryService.updateOnt(id, ont);
            return ResponseEntity.ok(dtoMapper.toDto(updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/onts/{id}")
    public ResponseEntity<Void> deleteOnt(@PathVariable Long id) {
        try {
            inventoryService.deleteOnt(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== PM/PBO ENDPOINTS ==========

    @GetMapping("/pm-pbo")
    public List<PMPBODTO> getAllPmPbo() {
        return dtoMapper.toPmPboDtoList(inventoryService.getAllPmPbo());
    }

    @GetMapping("/pm-pbo/{id}")
    public ResponseEntity<PMPBODTO> getPmPboById(@PathVariable Long id) {
        return inventoryService.getPmPboById(id)
                .map(dtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pm-pbo/type/{type}")
    public List<PMPBODTO> getPmPboByType(@PathVariable String type) {
        return dtoMapper.toPmPboDtoList(inventoryService.getPmPboByType(type));
    }

    @PostMapping("/pm-pbo")
    public ResponseEntity<?> createPmPbo(@Valid @RequestBody PmPboCreateDto pmPboDto) {
        try {
            PM_PBO pmPbo = new PM_PBO();
            pmPbo.setType(pmPboDto.getType());
            pmPbo.setCapacity(pmPboDto.getCapacity());
            pmPbo.setLocation(pmPboDto.getLocation());

            PM_PBO created = inventoryService.createPmPbo(pmPbo);
            return new ResponseEntity<>(dtoMapper.toDto(created), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/pm-pbo/{id}")
    public ResponseEntity<?> updatePmPbo(@PathVariable Long id, @Valid @RequestBody PmPboCreateDto pmPboDto) {
        try {
            PM_PBO pmPbo = new PM_PBO();
            pmPbo.setType(pmPboDto.getType());
            pmPbo.setCapacity(pmPboDto.getCapacity());
            pmPbo.setLocation(pmPboDto.getLocation());

            PM_PBO updated = inventoryService.updatePmPbo(id, pmPbo);
            return ResponseEntity.ok(dtoMapper.toDto(updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/pm-pbo/{id}")
    public ResponseEntity<Void> deletePmPbo(@PathVariable Long id) {
        try {
            inventoryService.deletePmPbo(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    // ========== INCIDENT ENDPOINTS ==========

    @Autowired
    private IncidentRepository incidentRepository;

    @GetMapping("/incidents")
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    @GetMapping("/incidents/paginated")
    public ResponseEntity<Page<Incident>> getIncidentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Incident> incidentPage = incidentRepository.findAll(pageable);
        return ResponseEntity.ok(incidentPage);
    }

    @GetMapping("/incidents/{id}")
    public ResponseEntity<Incident> getIncidentById(@PathVariable Long id) {
        return incidentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/incidents")
    public ResponseEntity<?> createIncident(@Valid @RequestBody IncidentCreateDto incidentDto) {
        try {
            Incident incident = new Incident();
            incident.setResource(incidentDto.getResource());
            incident.setValue(incidentDto.getValue());
            incident.setType(incidentDto.getType());
            incident.setRecommendation(incidentDto.getRecommendation());
            incident.setStatus("PENDING");

            Incident created = incidentRepository.save(incident);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/incidents/{id}")
    public ResponseEntity<?> updateIncident(@PathVariable Long id, @Valid @RequestBody IncidentUpdateDto incidentDto) {
        try {
            Incident incident = incidentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

            if (incidentDto.getStatus() != null) {
                incident.setStatus(incidentDto.getStatus());
            }
            if (incidentDto.getRecommendation() != null) {
                incident.setRecommendation(incidentDto.getRecommendation());
            }

            Incident updated = incidentRepository.save(incident);
            return ResponseEntity.ok(updated);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/incidents/{id}/status")
    public ResponseEntity<?> updateIncidentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            if (status == null || (!status.equals("PENDING") && !status.equals("IN_PROGRESS") && !status.equals("RESOLVED"))) {
                return ResponseEntity.badRequest().body("Statut invalide. Utilisez: PENDING, IN_PROGRESS, RESOLVED");
            }

            Incident incident = incidentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

            incident.setStatus(status);
            Incident updated = incidentRepository.save(incident);
            return ResponseEntity.ok(updated);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/incidents/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        try {
            if (!incidentRepository.existsById(id)) {
                throw new ResourceNotFoundException("Incident", "id", id);
            }
            incidentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}