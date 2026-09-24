package pi.stagepfesotetel.controllers;

import pi.stagepfesotetel.dto.MonitoringSummaryDTO;
import pi.stagepfesotetel.services.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    @Autowired
    private MonitoringService monitoringService;

    @GetMapping("/pon/{id}/summary")
    public ResponseEntity<MonitoringSummaryDTO> getPonSummary(@PathVariable Long id) {
        MonitoringSummaryDTO summary = monitoringService.getPonSummary(id);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/network/summary")
    public ResponseEntity<Map<String, Object>> getNetworkSummary() {
        return ResponseEntity.ok(monitoringService.getNetworkSummary());
    }

    @GetMapping("/olt/{id}/summary")
    public ResponseEntity<Map<String, Object>> getOltSummary(@PathVariable Long id) {
        return ResponseEntity.ok(monitoringService.getOltSummary(id));
    }

    @GetMapping("/splitter/{id}/summary")
    public ResponseEntity<Map<String, Object>> getSplitterSummary(@PathVariable Long id) {
        return ResponseEntity.ok(monitoringService.getSplitterSummary(id));
    }

    @GetMapping("/pon/{id}/topology")
    public ResponseEntity<Map<String, Object>> getPonTopology(@PathVariable Long id) {
        return ResponseEntity.ok(monitoringService.getPonTopology(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(monitoringService.getDashboardStats());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Monitoring Service is UP");
    }
}