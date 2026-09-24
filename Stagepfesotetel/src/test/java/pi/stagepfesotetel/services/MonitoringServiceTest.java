package pi.stagepfesotetel.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pi.stagepfesotetel.dto.MonitoringSummaryDTO;
import pi.stagepfesotetel.entities.PON;
import pi.stagepfesotetel.exceptions.ResourceNotFoundException;
import pi.stagepfesotetel.repositories.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @InjectMocks
    private MonitoringService monitoringService;

    @Mock
    private OLTRepository oltRepository;

    @Mock
    private PONRepository ponRepository;

    @Mock
    private SplitterRepository splitterRepository;

    @Mock
    private ONTRepository ontRepository;

    @Mock
    private PM_PBORepository pmPboRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Test
    void testGetPonSummary_ValidPon_ReturnsAggregatedData() {
        Long ponId = 1L;
        PON mockPon = new PON();
        mockPon.setId(ponId);
        mockPon.setPortIndex(2);
        mockPon.setTxPower(-15.0);

        when(ponRepository.findById(ponId)).thenReturn(Optional.of(mockPon));
        when(splitterRepository.findByParentPonId(ponId)).thenReturn(java.util.List.of());

        MonitoringSummaryDTO summary = monitoringService.getPonSummary(ponId);

        assertNotNull(summary, "Summary should not be null");
        assertEquals(ponId, summary.getPonId(), "PON ID should match");
        assertEquals(2, summary.getPortIndex(), "Port index should match");
        assertEquals(-15.0, summary.getTxPower(), "TX power should match");
    }

    @Test
    void testGetPonSummary_InvalidPon_ThrowsException() {
        Long ponId = 99L;

        when(ponRepository.findById(ponId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> monitoringService.getPonSummary(ponId));
        assertEquals("PON non trouvé avec id : '99'", exception.getMessage(), "Exception message should match");
    }

    @Test
    void testGetNetworkSummary_ReturnsCountsAndHealthPercentage() {
        when(oltRepository.count()).thenReturn(2L);
        when(ponRepository.count()).thenReturn(4L);
        when(splitterRepository.count()).thenReturn(8L);
        when(ontRepository.count()).thenReturn(10L);
        when(pmPboRepository.count()).thenReturn(3L);
        when(incidentRepository.count()).thenReturn(1L);
        when(ontRepository.countByStatus("online")).thenReturn(8L);
        when(ontRepository.countByStatus("offline")).thenReturn(1L);
        when(ontRepository.countByStatus("degraded")).thenReturn(1L);
        when(ontRepository.findCriticalRxPower()).thenReturn(java.util.List.of());

        java.util.Map<String, Object> summary = monitoringService.getNetworkSummary();

        assertEquals(10L, summary.get("totalOnts"), "The total ONT count must come from the repository");
        assertEquals(80.0, summary.get("healthPercentage"), "Health must be calculated from online ONTs");
        assertEquals(0, summary.get("criticalOnts"), "An empty critical list must produce zero critical ONTs");
    }

    @Test
    void testGetPonSummary_WithNoSplitters_ReturnsZeroOntCounts() {
        PON pon = new PON();
        pon.setId(2L);
        pon.setPortIndex(3);
        pon.setTxPower(2.0);
        when(ponRepository.findById(2L)).thenReturn(Optional.of(pon));
        when(splitterRepository.findByParentPonId(2L)).thenReturn(java.util.List.of());

        MonitoringSummaryDTO summary = monitoringService.getPonSummary(2L);

        assertEquals(0, summary.getOntCount(), "A PON without splitters must report no ONTs");
        assertEquals(0, summary.getSplitterCount(), "A PON without splitters must report zero splitters");
    }
}
