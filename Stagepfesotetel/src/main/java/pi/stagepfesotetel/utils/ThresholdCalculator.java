package pi.stagepfesotetel.utils;

import java.util.HashMap;
import java.util.Map;

public class ThresholdCalculator {

    // Pertes théoriques des splitters (dB)
    private static final Map<Integer, Double> SPLITTER_LOSS_THEORETICAL = new HashMap<>();

    static {
        SPLITTER_LOSS_THEORETICAL.put(4, 7.2);   // Splitter 1:4  → perte ~7.2 dB
        SPLITTER_LOSS_THEORETICAL.put(8, 10.5);  // Splitter 1:8  → perte ~10.5 dB
        SPLITTER_LOSS_THEORETICAL.put(16, 13.8); // Splitter 1:16 → perte ~13.8 dB
        SPLITTER_LOSS_THEORETICAL.put(32, 17.1); // Splitter 1:32 → perte ~17.1 dB
    }

    // Perte fibre optique par km (dB/km)
    private static final double FIBER_LOSS_PER_KM = 0.35;

    public static double calculateTheoreticalLoss(int splitterRatio, double distanceKm) {
        double splitterLoss = SPLITTER_LOSS_THEORETICAL.getOrDefault(splitterRatio, 15.0);
        double fiberLoss = distanceKm * FIBER_LOSS_PER_KM;
        return splitterLoss + fiberLoss;
    }

    public static double calculateOpticalBudget(double txPower, double rxPower) {
        return txPower - rxPower;
    }

    public static double calculateLossDeviation(double theoreticalLoss, double actualLoss) {
        return actualLoss - theoreticalLoss;
    }
}