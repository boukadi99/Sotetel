package pi.stagepfesotetel.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pi.stagepfesotetel.entities.ONT;
import pi.stagepfesotetel.repositories.ONTRepository;
import pi.stagepfesotetel.services.AnomalyDetectionService;

import java.util.Optional;

@RestController
@RequestMapping("/api/ml")
public class MLController {

    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

    @Autowired
    private ONTRepository ontRepository;

    /**
     * Endpoint to trigger model training
     */
    @PostMapping("/train")
    public ResponseEntity<String> trainModel() {
        anomalyDetectionService.trainModel();
        return ResponseEntity.ok("Model training initiated.");
    }

    /**
     * Endpoint to get anomaly score for a specific ONT
     */
    @GetMapping("/anomaly/{ontId}")
    public ResponseEntity<?> getAnomalyScore(@PathVariable Long ontId) {
        Optional<ONT> ontOptional = ontRepository.findById(ontId);

        if (ontOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("ONT not found with id: " + ontId);
        }

        ONT ont = ontOptional.get();
        Optional<AnomalyDetectionService.AnomalyResult> resultOpt =
                anomalyDetectionService.scoreONT(ont);

        if (resultOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Model not trained yet.");
        }

        return ResponseEntity.ok(resultOpt.get());
    }
}