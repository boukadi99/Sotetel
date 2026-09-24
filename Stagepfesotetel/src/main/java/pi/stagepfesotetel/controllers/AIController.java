package pi.stagepfesotetel.controllers;

import pi.stagepfesotetel.dto.AIDiagnosticRequest;
import pi.stagepfesotetel.dto.AIDiagnosticResponse;
import pi.stagepfesotetel.services.AIService;
import pi.stagepfesotetel.exceptions.ResourceNotFoundException;
import pi.stagepfesotetel.utils.ExpertRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;
@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/diagnostics")
    public ResponseEntity<AIDiagnosticResponse> diagnose(@Valid @RequestBody AIDiagnosticRequest request) {
        try {
            AIDiagnosticResponse response = aiService.diagnose(request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            AIDiagnosticResponse error = new AIDiagnosticResponse();
            error.setStatus("NOT_FOUND");
            error.setConfidenceScore(0);
            return ResponseEntity.status(404).body(error);
        }
    }

    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> getRules() {
        Map<String, Object> rules = new HashMap<>();

        rules.put("rxThresholds", Map.of(
                "GPON", Map.of("critical", -28.0, "degraded", -25.0, "warning", -22.0, "good", -18.0),
                "XGS-PON", Map.of("critical", -26.0, "degraded", -23.0, "warning", -20.0, "good", -16.0)
        ));

        rules.put("txThresholds", Map.of(
                "critical", 0.3,
                "degraded", 0.8,
                "warning", 1.2,
                "good", 1.8
        ));

        rules.put("splitterLoss", ExpertRules.SPLITTER_LOSS);
        rules.put("fiberLossPerKm", 0.35);

        return ResponseEntity.ok(rules);
    }
}