package pi.stagepfesotetel.controllers;

import pi.stagepfesotetel.dto.ThresholdDto;
import pi.stagepfesotetel.services.ThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/api/thresholds")
public class ThresholdController {

    @Autowired
    private ThresholdService thresholdService;

    @PostConstruct
    public void init() {
        thresholdService.initDefaultThresholds();
    }

    @GetMapping
    public ResponseEntity<List<ThresholdDto>> getAllThresholds() {
        return ResponseEntity.ok(thresholdService.getAllThresholds());
    }

    @GetMapping("/{name}/{technology}")
    public ResponseEntity<ThresholdDto> getThreshold(@PathVariable String name, @PathVariable String technology) {
        ThresholdDto dto = thresholdService.getThreshold(name, technology);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThresholdDto> updateThreshold(@PathVariable Long id, @RequestBody ThresholdDto dto) {
        return ResponseEntity.ok(thresholdService.updateThreshold(id, dto));
    }
}