package pi.stagepfesotetel.services;

import pi.stagepfesotetel.entities.Threshold;
import pi.stagepfesotetel.dto.ThresholdDto;
import pi.stagepfesotetel.repositories.ThresholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThresholdService {

    @Autowired
    private ThresholdRepository thresholdRepository;

    public List<ThresholdDto> getAllThresholds() {
        return thresholdRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ThresholdDto getThreshold(String name, String technology) {
        return thresholdRepository.findByNameAndTechnology(name, technology)
                .map(this::toDto)
                .orElse(null);
    }

    public ThresholdDto updateThreshold(Long id, ThresholdDto dto) {
        Threshold threshold = thresholdRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seuil non trouvé"));

        threshold.setValue(dto.getValue());
        threshold.setDescription(dto.getDescription());

        return toDto(thresholdRepository.save(threshold));
    }

    public void initDefaultThresholds() {
        if (thresholdRepository.count() == 0) {
            // RX Thresholds GPON
            saveThreshold("rx_critical", "GPON", -28.0, "dBm", "RX critique GPON");
            saveThreshold("rx_degraded", "GPON", -25.0, "dBm", "RX dégradé GPON");
            saveThreshold("rx_warning", "GPON", -22.0, "dBm", "RX warning GPON");
            saveThreshold("rx_good", "GPON", -18.0, "dBm", "RX bon GPON");

            // RX Thresholds XGS-PON
            saveThreshold("rx_critical", "XGS-PON", -26.0, "dBm", "RX critique XGS-PON");
            saveThreshold("rx_degraded", "XGS-PON", -23.0, "dBm", "RX dégradé XGS-PON");
            saveThreshold("rx_warning", "XGS-PON", -20.0, "dBm", "RX warning XGS-PON");
            saveThreshold("rx_good", "XGS-PON", -16.0, "dBm", "RX bon XGS-PON");

            // TX Thresholds (génériques)
            saveThreshold("tx_critical", "DEFAULT", 0.3, "dBm", "TX critique");
            saveThreshold("tx_degraded", "DEFAULT", 0.8, "dBm", "TX dégradé");
            saveThreshold("tx_warning", "DEFAULT", 1.2, "dBm", "TX warning");
            saveThreshold("tx_good", "DEFAULT", 1.8, "dBm", "TX bon");

            // Splitter loss
            saveThreshold("splitter_loss_4", "SPLITTER", 7.2, "dB", "Perte splitter 1:4");
            saveThreshold("splitter_loss_8", "SPLITTER", 10.5, "dB", "Perte splitter 1:8");
            saveThreshold("splitter_loss_16", "SPLITTER", 13.8, "dB", "Perte splitter 1:16");
            saveThreshold("splitter_loss_32", "SPLITTER", 17.1, "dB", "Perte splitter 1:32");

            // Fiber loss
            saveThreshold("fiber_loss_per_km", "DEFAULT", 0.35, "dB/km", "Atténuation fibre par km");
            saveThreshold("loss_deviation_warning", "DEFAULT", 3.0, "dB", "Déviation de perte warning");
        }
    }

    private void saveThreshold(String name, String technology, Double value, String unit, String description) {
        Threshold t = new Threshold(name, technology, value, unit, description);
        thresholdRepository.save(t);
    }

    private ThresholdDto toDto(Threshold threshold) {
        ThresholdDto dto = new ThresholdDto();
        dto.setId(threshold.getId());
        dto.setName(threshold.getName());
        dto.setTechnology(threshold.getTechnology());
        dto.setValue(threshold.getValue());
        dto.setUnit(threshold.getUnit());
        dto.setDescription(threshold.getDescription());
        return dto;
    }
}