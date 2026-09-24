package pi.stagepfesotetel.utils;

import java.util.HashMap;
import java.util.Map;

public class ExpertRules {

    // ========== SEUILS RX PAR TECHNOLOGIE ==========
    public static final Map<String, double[]> RX_THRESHOLDS = new HashMap<>();

    static {
        // [critique, dégradé, warning, bon]
        RX_THRESHOLDS.put("GPON", new double[]{-28.0, -25.0, -22.0, -18.0});
        RX_THRESHOLDS.put("XGS-PON", new double[]{-26.0, -23.0, -20.0, -16.0});
        RX_THRESHOLDS.put("DEFAULT", new double[]{-27.0, -25.0, -20.0, -15.0});
    }

    // ========== SEUILS TX ==========
    public static final double TX_CRITICAL = 0.3;
    public static final double TX_DEGRADED = 0.8;
    public static final double TX_WARNING = 1.2;
    public static final double TX_GOOD = 1.8;

    // ========== PERTES THÉORIQUES DES SPLITTERS ==========
    public static final Map<Integer, Double> SPLITTER_LOSS = new HashMap<>();

    static {
        SPLITTER_LOSS.put(4, 7.2);
        SPLITTER_LOSS.put(8, 10.5);
        SPLITTER_LOSS.put(16, 13.8);
        SPLITTER_LOSS.put(32, 17.1);
    }

    // ========== PERTE FIBRE PAR KM ==========
    public static final double FIBER_LOSS_PER_KM = 0.35;

    // ========== RÈGLES DE DIAGNOSTIC ==========

    public static String evaluateRxPower(double rxPower, String technology) {
        double[] thresholds = RX_THRESHOLDS.getOrDefault(technology, RX_THRESHOLDS.get("DEFAULT"));

        if (rxPower < thresholds[0]) return "CRITICAL";
        if (rxPower < thresholds[1]) return "DEGRADED";
        if (rxPower < thresholds[2]) return "WARNING";
        return "GOOD";
    }

    public static String evaluateTxPower(double txPower) {
        if (txPower < TX_CRITICAL) return "CRITICAL";
        if (txPower < TX_DEGRADED) return "DEGRADED";
        if (txPower < TX_WARNING) return "WARNING";
        return "GOOD";
    }

    public static double calculateLossDeviation(double txPower, double rxPower, int splitterRatio, double distanceKm) {
        double actualLoss = txPower - rxPower;
        double theoreticalLoss = SPLITTER_LOSS.getOrDefault(splitterRatio, 15.0) + (distanceKm * FIBER_LOSS_PER_KM);
        return actualLoss - theoreticalLoss;
    }

    public static String getRecommendation(String anomalyType, double severity) {
        return switch (anomalyType) {
            case "RX_CRITICAL" -> "Remplacer le SFP de l'ONT et vérifier la connectique fibre";
            case "RX_DEGRADED" -> "Inspecter le connecteur et nettoyer la fibre";
            case "RX_WARNING" -> "Surveiller l'évolution de la puissance RX";
            case "TX_CRITICAL" -> "Remplacer l'ONT (émetteur défaillant)";
            case "TX_DEGRADED" -> "Vérifier l'alimentation de l'ONT";
            case "OFFLINE" -> "Vérifier l'alimentation et redémarrer l'ONT";
            case "LOSS_HIGH" -> "Vérifier les épissures et connecteurs";
            default -> "Aucune action recommandée";
        };
    }
}