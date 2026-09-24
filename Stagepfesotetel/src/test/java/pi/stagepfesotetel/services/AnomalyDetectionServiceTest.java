package pi.stagepfesotetel.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pi.stagepfesotetel.entities.ONT;
import pi.stagepfesotetel.repositories.ONTRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {
    @InjectMocks private AnomalyDetectionService anomalyDetectionService;
    @Mock private ONTRepository ontRepository;

    @Test
    void scoreONT_beforeTraining_returnsEmpty() {
        assertTrue(anomalyDetectionService.scoreONT(ont(1)).isEmpty(), "Scoring without a model must return no result");
    }

    @Test
    void trainModel_fewerThanFiftyOnts_doesNotCreateModel() {
        when(ontRepository.findAll()).thenReturn(IntStream.range(0, 49).mapToObj(this::ont).toList());
        anomalyDetectionService.trainModel();
        assertTrue(anomalyDetectionService.scoreONT(ont(50)).isEmpty(), "The service must enforce the 50-ONT training minimum");
        verify(ontRepository).findAll();
    }

    @Test
    void trainModel_fiftyOnts_allowsScoring() {
        List<ONT> trainingData = IntStream.range(0, 50).mapToObj(this::ont).toList();
        when(ontRepository.findAll()).thenReturn(trainingData);
        anomalyDetectionService.trainModel();
        Optional<AnomalyDetectionService.AnomalyResult> result = anomalyDetectionService.scoreONT(trainingData.get(0));
        assertTrue(result.isPresent(), "A trained model must produce an anomaly result");
        assertFalse(result.orElseThrow().getClassification().isBlank(), "The anomaly result must include a classification");
    }

    private ONT ont(int id) {
        ONT ont = new ONT();
        ont.setId((long) id); ont.setRxPower(-20.0 - id * 0.01); ont.setTxPower(1.0 + id * 0.01);
        ont.setDistanceKm(5.0 + id * 0.1); ont.setStatus("online");
        return ont;
    }
}
