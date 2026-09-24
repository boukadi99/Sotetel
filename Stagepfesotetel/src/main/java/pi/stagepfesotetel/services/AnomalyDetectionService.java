package pi.stagepfesotetel.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pi.stagepfesotetel.entities.ONT;
import pi.stagepfesotetel.repositories.ONTRepository;
import smile.anomaly.IsolationForest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class
AnomalyDetectionService {

    private static final Logger logger = Logger.getLogger(AnomalyDetectionService.class.getName());

    @Autowired
    private ONTRepository ontRepository;

    private IsolationForest model;

    private double[] minValues;
    private double[] maxValues;

    public void trainModel() {
        List<ONT> ontData = ontRepository.findAll();

        if (ontData.size() < 50) {
            logger.warning("Not enough data to train the model. Minimum 50 ONTs required.");
            return;
        }

        double[][] rawData = ontData.stream()
                .map(ont -> new double[]{ont.getRxPower(), ont.getTxPower(), ont.getDistanceKm()})
                .toArray(double[][]::new);

        int numFeatures = rawData[0].length;
        minValues = new double[numFeatures];
        maxValues = new double[numFeatures];

        for (int i = 0; i < numFeatures; i++) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (double[] row : rawData) {
                if (row[i] < min) min = row[i];
                if (row[i] > max) max = row[i];
            }
            minValues[i] = min;
            maxValues[i] = max;
        }

        double[][] normalizedData = new double[rawData.length][numFeatures];
        for (int i = 0; i < rawData.length; i++) {
            for (int j = 0; j < numFeatures; j++) {
                double range = maxValues[j] - minValues[j];
                normalizedData[i][j] = range > 0 ? (rawData[i][j] - minValues[j]) / range : 0;
            }
        }

        long startTime = System.currentTimeMillis();
        model = IsolationForest.fit(normalizedData);
        long endTime = System.currentTimeMillis();

        logger.info("Model trained with " + normalizedData.length + " samples and " + numFeatures + " features in " + (endTime - startTime) + " ms.");
        logger.info("Feature ranges: " + Arrays.toString(minValues) + " to " + Arrays.toString(maxValues));
    }

    public Optional<AnomalyResult> scoreONT(ONT ont) {
        if (model == null) {
            logger.warning("Model not trained yet.");
            return Optional.empty();
        }

        double[] rawFeatures = {ont.getRxPower(), ont.getTxPower(), ont.getDistanceKm()};

        // Safe normalization
        double[] normalizedFeatures = new double[rawFeatures.length];
        for (int i = 0; i < rawFeatures.length; i++) {
            double range = maxValues[i] - minValues[i];
            normalizedFeatures[i] = range > 0 ? (rawFeatures[i] - minValues[i]) / range : 0;
        }

        // ML score from Isolation Forest (used for logging + as fallback signal)
        double[] rawScores = model.score(new double[][]{normalizedFeatures});
        double isolationScore = rawScores[0];

        // ============================================
        // ✅ DIRECTIONAL ANOMALY SCORE
        // For fiber optics, "bad" = low RX, low TX, high distance
        // ============================================
        double rxNorm = normalizedFeatures[0];       // 0 = worst RX, 1 = best RX
        double txNorm = normalizedFeatures[1];       // 0 = worst TX, 1 = best TX
        double distNorm = normalizedFeatures[2];     // 0 = closest, 1 = farthest

        // Badness = 1 - goodness (for RX, TX); distance is already "badness"
        double rxBadness = 1.0 - rxNorm;
        double txBadness = 1.0 - txNorm;
        double distBadness = distNorm;

        // Weighted badness score (RX is most important)
        double badness = 0.6 * rxBadness + 0.3 * txBadness + 0.1 * distBadness;

        double score = Math.max(0, Math.min(1, badness));

        String classification;
        if (score > 0.6) {
            classification = "anomaly";
        } else if (score > 0.4) {
            classification = "suspicious";
        } else {
            classification = "normal";
        }

        logger.info("ONT scored: isolation=" + String.format("%.4f", isolationScore)
                + " rxBad=" + String.format("%.2f", rxBadness)
                + " txBad=" + String.format("%.2f", txBadness)
                + " distBad=" + String.format("%.2f", distBadness)
                + " → final=" + String.format("%.4f", score)
                + " → " + classification);

        return Optional.of(new AnomalyResult(score, classification));
    }

    @Scheduled(fixedRate = 86400000) // Every 24 hours
    public void scheduledRetraining() {
        logger.info("Scheduled retraining started.");
        trainModel();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void autoTrainOnStartup() {
        try {
            Thread.sleep(2000); // Ensure DataLoaderService finishes first
            logger.info("🤖 Auto-training ML model on startup...");
            trainModel();
        } catch (Exception e) {
            logger.severe("Failed to auto-train ML model on startup: " + e.getMessage());
        }
    }

    // ============================================
    // AnomalyResult
    // ============================================
    public static class AnomalyResult {
        private final double score;
        private final String classification;

        public AnomalyResult(double score, String classification) {
            this.score = score;
            this.classification = classification;
        }

        public double getScore() {
            return score;
        }

        public String getClassification() {
            return classification;
        }

        public boolean isAnomaly() {
            return "anomaly".equals(classification);
        }

        public boolean isSuspicious() {
            return "suspicious".equals(classification);
        }
    }
}