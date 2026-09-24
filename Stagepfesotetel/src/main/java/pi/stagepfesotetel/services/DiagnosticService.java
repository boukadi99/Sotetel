package pi.stagepfesotetel.services;

import pi.stagepfesotetel.entities.ONT;
import pi.stagepfesotetel.repositories.ONTRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DiagnosticService {

    @Autowired
    private ONTRepository ontRepository;

    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

    // Seuils de diagnostic (constants)
    private static final double RX_THRESHOLD_LOW = -25.0;
    private static final double TX_THRESHOLD_LOW = 0.5;

    /**
     * Diagnostique un ONT spécifique
     */
    public Map<String, Object> diagnoseOnt(Long id) {
        Map<String, Object> result = new HashMap<>();

        ONT ont = ontRepository.findById(id).orElse(null);
        if (ont == null) {
            result.put("error", "ONT not found with id: " + id);
            return result;
        }

        // Informations de base
        result.put("ontId", ont.getId());
        result.put("serial", ont.getSerial());
        result.put("status", ont.getStatus());
        result.put("rxPower", ont.getRxPower());
        result.put("txPower", ont.getTxPower());
        result.put("distanceKm", ont.getDistanceKm());

        // Diagnostic
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // Vérification RX Power
        if (ont.getRxPower() < RX_THRESHOLD_LOW) {
            issues.add("RX power too low: " + ont.getRxPower() + " dBm");
            recommendations.add("Check fiber connection at ONT");
            recommendations.add("Inspect splitter port");
            recommendations.add("Consider cleaning fiber end");
        }

        // Vérification TX Power
        if (ont.getTxPower() < TX_THRESHOLD_LOW) {
            issues.add("TX power too low: " + ont.getTxPower() + " dBm");
            recommendations.add("Check ONT transmitter");
            recommendations.add("Verify power supply to ONT");
            recommendations.add("Consider replacing ONT if issue persists");
        }

        // Vérification du statut
        if ("offline".equals(ont.getStatus())) {
            issues.add("ONT is offline");
            recommendations.add("Check power supply at customer premises");
            recommendations.add("Verify OLT connectivity");
            recommendations.add("Restart the ONT");
        } else if ("degraded".equals(ont.getStatus())) {
            issues.add("ONT is in degraded state");
            recommendations.add("Monitor signal levels for 24 hours");
            recommendations.add("Schedule preventive maintenance");
            recommendations.add("Check for any recent changes in the network");
        }

        // Vérification de la distance (si anormale)
        if (ont.getDistanceKm() > 20) {
            issues.add("ONT distance is high: " + ont.getDistanceKm() + " km");
            recommendations.add("Verify if ONT is within acceptable range");
            recommendations.add("Check for additional splitters in the path");
        }

        // Détection d'anomalies par ML
        anomalyDetectionService.scoreONT(ont).ifPresent(anomalyResult -> {
            result.put("anomalyScore", anomalyResult.getScore());
            result.put("isAnomaly", anomalyResult.isAnomaly());

            if (anomalyResult.isAnomaly()) {
                issues.add("Anomaly detected by ML model.");
                recommendations.add("Investigate ONT for potential issues.");
            }
        });

        // Si aucun problème détecté
        if (issues.isEmpty()) {
            issues.add("No issues detected");
            recommendations.add("ONT is operating normally");
            recommendations.add("Continue regular monitoring");
        }

        result.put("issues", issues);
        result.put("recommendations", recommendations);
        result.put("issueCount", issues.size());

        // Calcul d'un score de santé (0-100)
        int healthScore = calculateHealthScore(ont, issues.size());
        result.put("healthScore", healthScore);

        return result;
    }

    /**
     * Calcule le résumé statistique du réseau
     */
    public Map<String, Object> getNetworkSummary() {
        Map<String, Object> summary = new HashMap<>();

        long totalOnts = ontRepository.count();
        long onlineOnts = ontRepository.countByStatus("online");
        long offlineOnts = ontRepository.countByStatus("offline");
        long degradedOnts = ontRepository.countByStatus("degraded");

        summary.put("totalOnts", totalOnts);
        summary.put("onlineOnts", onlineOnts);
        summary.put("offlineOnts", offlineOnts);
        summary.put("degradedOnts", degradedOnts);

        // Pourcentage de santé
        double healthyPercentage = totalOnts > 0 ?
                (onlineOnts * 100.0 / totalOnts) : 0;
        summary.put("healthyPercentage", Math.round(healthyPercentage * 10) / 10.0);

        // ONTs critiques (RX trop faible)
        long criticalOnts = ontRepository.findCriticalRxPower().size();
        summary.put("criticalOnts", criticalOnts);

        // Taux de défaillance
        double failureRate = totalOnts > 0 ?
                ((offlineOnts + degradedOnts) * 100.0 / totalOnts) : 0;
        summary.put("failureRate", Math.round(failureRate * 10) / 10.0);

        return summary;
    }

    /**
     * Calcule un score de santé pour un ONT
     */
    private int calculateHealthScore(ONT ont, int issueCount) {
        int score = 100;

        // Pénalités basées sur les problèmes
        if (ont.getRxPower() < RX_THRESHOLD_LOW) score -= 30;
        if (ont.getTxPower() < TX_THRESHOLD_LOW) score -= 30;
        if ("offline".equals(ont.getStatus())) score -= 50;
        if ("degraded".equals(ont.getStatus())) score -= 20;
        if (ont.getDistanceKm() > 20) score -= 10;

        // Réduction supplémentaire basée sur le nombre de problèmes
        score -= issueCount * 5;

        return Math.max(0, Math.min(100, score)); // Entre 0 et 100
    }
}