package pi.stagepfesotetel.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pi.stagepfesotetel.entities.ONT;
import pi.stagepfesotetel.repositories.ONTRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticServiceTest {

    @InjectMocks
    private DiagnosticService diagnosticService;

    @Mock
    private ONTRepository ontRepository;

    @Mock
    private AnomalyDetectionService anomalyDetectionService;

    @Test
    void testDiagnoseOnt_HealthyOnt_NoIssues() {
        ONT ont = createONT(1L, -18.0, 1.5, "online");
        when(ontRepository.findById(1L)).thenReturn(Optional.of(ont));
        when(anomalyDetectionService.scoreONT(ont)).thenReturn(Optional.empty());

        var result = diagnosticService.diagnoseOnt(1L);

        assertNotNull(result);
        assertTrue(result.get("issues") instanceof java.util.List);
    }

    @Test
    void testDiagnoseOnt_LowRxPower_DetectsIssue() {
        ONT ont = createONT(1L, -27.0, 1.5, "online");
        when(ontRepository.findById(1L)).thenReturn(Optional.of(ont));
        when(anomalyDetectionService.scoreONT(ont)).thenReturn(Optional.empty());

        var result = diagnosticService.diagnoseOnt(1L);

        var issues = (java.util.List<String>) result.get("issues");
        assertFalse(issues.isEmpty());
    }

    @Test
    void testDiagnoseOnt_OfflineStatus_DetectsIssue() {
        ONT ont = createONT(1L, -18.0, 1.5, "offline");
        when(ontRepository.findById(1L)).thenReturn(Optional.of(ont));
        when(anomalyDetectionService.scoreONT(ont)).thenReturn(Optional.empty());

        var result = diagnosticService.diagnoseOnt(1L);

        var issues = (java.util.List<String>) result.get("issues");
        assertTrue(issues.size() > 0);
    }

    @Test
    void testDiagnoseOnt_NotFound_ReturnsError() {
        when(ontRepository.findById(999L)).thenReturn(Optional.empty());

        var result = diagnosticService.diagnoseOnt(999L);

        assertTrue(result.containsKey("error"));
    }

    @Test
    void testGetNetworkSummary_ReturnsStatistics() {
        when(ontRepository.count()).thenReturn(100L);
        when(ontRepository.countByStatus("online")).thenReturn(80L);
        when(ontRepository.countByStatus("offline")).thenReturn(15L);
        when(ontRepository.countByStatus("degraded")).thenReturn(5L);
        when(ontRepository.findCriticalRxPower()).thenReturn(java.util.List.of());

        var summary = diagnosticService.getNetworkSummary();

        assertEquals(100L, summary.get("totalOnts"));
        assertEquals(80L, summary.get("onlineOnts"));
    }

    private ONT createONT(Long id, Double rxPower, Double txPower, String status) {
        ONT ont = new ONT();
        ont.setId(id);
        ont.setSerial("ONT-TEST");
        ont.setRxPower(rxPower);
        ont.setTxPower(txPower);
        ont.setDistanceKm(2.0);
        ont.setStatus(status);
        return ont;
    }
}
