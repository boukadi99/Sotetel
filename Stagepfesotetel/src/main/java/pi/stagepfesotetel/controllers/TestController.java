package pi.stagepfesotetel.controllers;

import pi.stagepfesotetel.entities.*;
import pi.stagepfesotetel.services.InventoryService;
import pi.stagepfesotetel.services.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private MonitoringService monitoringService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return monitoringService.getDashboardStats();  // ← CHANGER ICI
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "FiberNet-AI Pro",
                "timestamp", String.valueOf(System.currentTimeMillis())
        );
    }

    @GetMapping("/olts")
    public List<OLT> getAllOlts() {
        return inventoryService.getAllOlts();
    }

    @GetMapping("/pons")
    public List<PON> getAllPons() {
        return inventoryService.getAllPons();
    }

    @GetMapping("/splitters")
    public List<Splitter> getAllSplitters() {
        return inventoryService.getAllSplitters();
    }

    @GetMapping("/onts")
    public List<ONT> getAllOnts() {
        return inventoryService.getAllOnts();
    }

    @GetMapping("/pm-pbo")
    public List<PM_PBO> getAllPmPbo() {
        return inventoryService.getAllPmPbo();
    }

    @GetMapping("/incidents")
    public List<Incident> getAllIncidents() {
        // Retourne une liste vide pour l'instant
        return List.of();
    }
}