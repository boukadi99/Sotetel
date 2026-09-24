package pi.stagepfesotetel.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pi.stagepfesotetel.entities.Threshold;
import pi.stagepfesotetel.repositories.ThresholdRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdServiceTest {

    @InjectMocks
    private ThresholdService thresholdService;

    @Mock
    private ThresholdRepository thresholdRepository;

    @Test
    void testGetAllThresholds_ReturnsList() {
        Threshold t1 = new Threshold("rx_critical", "GPON", -28.0, "dBm", "RX critique");
        Threshold t2 = new Threshold("rx_good", "GPON", -18.0, "dBm", "RX bon");
        when(thresholdRepository.findAll()).thenReturn(List.of(t1, t2));

        var result = thresholdService.getAllThresholds();

        assertEquals(2, result.size());
    }

    @Test
    void testGetThresholdByName_ReturnsThreshold() {
        Threshold t = new Threshold("rx_critical", "GPON", -28.0, "dBm", "RX critique");
        when(thresholdRepository.findByNameAndTechnology("rx_critical", "GPON"))
                .thenReturn(Optional.of(t));

        var result = thresholdService.getThreshold("rx_critical", "GPON");

        assertNotNull(result);
        assertEquals(-28.0, result.getValue());
    }

    @Test
    void testGetThreshold_NotFound_ReturnsNull() {
        when(thresholdRepository.findByNameAndTechnology("unknown", "GPON"))
                .thenReturn(Optional.empty());

        var result = thresholdService.getThreshold("unknown", "GPON");

        assertNull(result);
    }
}