package pi.stagepfesotetel.controllers;

import pi.stagepfesotetel.services.DiagnosticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
public class DiagnosticController {

    @Autowired
    private DiagnosticService diagnosticService;

    @GetMapping("/ont/{id}")
    public ResponseEntity<Map<String, Object>> diagnoseOnt(@PathVariable Long id) {
        Map<String, Object> result = diagnosticService.diagnoseOnt(id);

        if (result.containsKey("error")) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<Map<String, Object>> getNetworkSummary() {
        return ResponseEntity.ok(diagnosticService.getNetworkSummary());
    }
}