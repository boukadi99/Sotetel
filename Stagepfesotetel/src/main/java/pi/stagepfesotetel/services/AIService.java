package pi.stagepfesotetel.services;

import pi.stagepfesotetel.dto.AIDiagnosticRequest;
import pi.stagepfesotetel.dto.AIDiagnosticResponse;
import pi.stagepfesotetel.dto.ThresholdDto;
import pi.stagepfesotetel.entities.*;
import pi.stagepfesotetel.exceptions.ResourceNotFoundException;
import pi.stagepfesotetel.repositories.*;
import pi.stagepfesotetel.utils.ExpertRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class AIService {

    @Autowired
    private ONTRepository ontRepository;

    @Autowired
    private OLTRepository oltRepository;

    @Autowired
    private SplitterRepository splitterRepository;

    @Autowired
    private PONRepository ponRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private ThresholdService thresholdService;

    // Cache pour les seuils (pour éviter les appels BDD à chaque diagnostic)
    private Map<String, Double> thresholdCache = new HashMap<>();

    @PostConstruct
    public void init() {
        loadThresholdsToCache();
    }

    private void loadThresholdsToCache() {
        // RX Thresholds
        thresholdCache.put("rx_critical_GPON", getThresholdValue("rx_critical", "GPON", -28.0));
        thresholdCache.put("rx_degraded_GPON", getThresholdValue("rx_degraded", "GPON", -25.0));
        thresholdCache.put("rx_warning_GPON", getThresholdValue("rx_warning", "GPON", -22.0));
        thresholdCache.put("rx_good_GPON", getThresholdValue("rx_good", "GPON", -18.0));

        thresholdCache.put("rx_critical_XGS-PON", getThresholdValue("rx_critical", "XGS-PON", -26.0));
        thresholdCache.put("rx_degraded_XGS-PON", getThresholdValue("rx_degraded", "XGS-PON", -23.0));
        thresholdCache.put("rx_warning_XGS-PON", getThresholdValue("rx_warning", "XGS-PON", -20.0));
        thresholdCache.put("rx_good_XGS-PON", getThresholdValue("rx_good", "XGS-PON", -16.0));

        // TX Thresholds
        thresholdCache.put("tx_critical", getThresholdValue("tx_critical", "DEFAULT", 0.3));
        thresholdCache.put("tx_degraded", getThresholdValue("tx_degraded", "DEFAULT", 0.8));
        thresholdCache.put("tx_warning", getThresholdValue("tx_warning", "DEFAULT", 1.2));
        thresholdCache.put("tx_good", getThresholdValue("tx_good", "DEFAULT", 1.8));

        // Loss deviation
        thresholdCache.put("loss_deviation_warning", getThresholdValue("loss_deviation_warning", "DEFAULT", 3.0));
    }

    private Double getThresholdValue(String name, String technology, double defaultValue) {
        try {
            ThresholdDto threshold = thresholdService.getThreshold(name, technology);
            return threshold != null ? threshold.getValue() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String evaluateRxPower(double rxPower, String technology) {
        String key = "rx_" + getRxLevel(rxPower, technology) + "_" + technology;
        return getRxLevel(rxPower, technology);
    }

    private String getRxLevel(double rxPower, String technology) {
        Double critical = thresholdCache.getOrDefault("rx_critical_" + technology, -28.0);
        Double degraded = thresholdCache.getOrDefault("rx_degraded_" + technology, -25.0);

        if (rxPower < critical) return "CRITICAL";
        if (rxPower < degraded) return "DEGRADED";
        return "GOOD";
    }

    private String evaluateTxPower(double txPower) {
        Double critical = thresholdCache.getOrDefault("tx_critical", 0.3);
        Double degraded = thresholdCache.getOrDefault("tx_degraded", 0.8);

        if (txPower < critical) return "CRITICAL";
        if (txPower < degraded) return "DEGRADED";
        return "GOOD";
    }

    private double getLossDeviationWarning() {
        return thresholdCache.getOrDefault("loss_deviation_warning", 3.0);
    }

    public AIDiagnosticResponse diagnose(AIDiagnosticRequest request) {
        return switch (request.getResourceType().toUpperCase()) {
            case "ONT" -> diagnoseONT(request);
            case "OLT" -> diagnoseOLT(request);
            case "SPLITTER" -> diagnoseSplitter(request);
            case "PON" -> diagnosePON(request);
            default -> {
                AIDiagnosticResponse error = new AIDiagnosticResponse();
                error.setStatus("ERROR");
                error.setConfidenceScore(0);
                yield error;
            }
        };
    }

    // ========== MÉTHODE POUR CHATBOT (RETOUR SIMPLE) ==========

    public Map<String, Object> diagnoseONT(Long ontId) {
        Map<String, Object> result = new HashMap<>();

        ONT ont = ontRepository.findById(ontId)
                .orElseThrow(() -> new ResourceNotFoundException("ONT", "id", ontId));

        result.put("ontId", ont.getId());
        result.put("serial", ont.getSerial());
        result.put("rxPower", ont.getRxPower());
        result.put("txPower", ont.getTxPower());
        result.put("distanceKm", ont.getDistanceKm());
        result.put("status", ont.getStatus());

        List<String> anomalies = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        String technology = "GPON";
        String rxLevel = getRxLevel(ont.getRxPower(), technology);
        String txLevel = evaluateTxPower(ont.getTxPower());
        double lossWarning = getLossDeviationWarning();

        // Diagnostic RX
        if ("CRITICAL".equals(rxLevel)) {
            anomalies.add("Puissance RX critique: " + ont.getRxPower() + " dBm");
            recommendations.add("Remplacer le SFP de l'ONT");
        } else if ("DEGRADED".equals(rxLevel)) {
            anomalies.add("Puissance RX dégradée: " + ont.getRxPower() + " dBm");
            recommendations.add("Inspecter le connecteur et nettoyer la fibre");
        }

        // Diagnostic TX
        if ("CRITICAL".equals(txLevel)) {
            anomalies.add("Puissance TX critique: " + ont.getTxPower() + " dBm");
            recommendations.add("Remplacer l'ONT");
        } else if ("DEGRADED".equals(txLevel)) {
            anomalies.add("Puissance TX dégradée: " + ont.getTxPower() + " dBm");
            recommendations.add("Vérifier l'alimentation de l'ONT");
        }

        // Diagnostic statut
        if ("offline".equals(ont.getStatus())) {
            anomalies.add("ONT hors ligne");
            recommendations.add("Vérifier l'alimentation électrique");
            recommendations.add("Redémarrer l'ONT");
        } else if ("degraded".equals(ont.getStatus())) {
            anomalies.add("ONT en état dégradé");
            recommendations.add("Planifier une maintenance préventive");
        }

        // Calcul de perte si splitter existe
        if (ont.getSplitter() != null) {
            double actualLoss = ont.getTxPower() - ont.getRxPower();
            double theoreticalLoss = ExpertRules.SPLITTER_LOSS.getOrDefault(ont.getSplitter().getRatio(), 15.0)
                    + (ont.getDistanceKm() * 0.35);
            double lossDeviation = actualLoss - theoreticalLoss;

            if (Math.abs(lossDeviation) > lossWarning) {
                anomalies.add("Perte anormale: " + String.format("%.2f", lossDeviation) + " dB");
                recommendations.add("Vérifier les épissures et connecteurs");
            }
        }

        if (anomalies.isEmpty()) {
            anomalies.add("Aucune anomalie détectée");
            recommendations.add("ONT fonctionne normalement");
        }

        result.put("anomalies", anomalies);
        result.put("recommendations", recommendations);

        return result;
    }

    private AIDiagnosticResponse diagnoseONT(AIDiagnosticRequest request) {
        AIDiagnosticResponse response = new AIDiagnosticResponse();
        response.setResourceId(request.getResourceId());
        response.setResourceType("ONT");

        ONT ont = ontRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("ONT", "id", request.getResourceId()));

        Map<String, Double> metrics = new HashMap<>();
        metrics.put("rxPower", ont.getRxPower());
        metrics.put("txPower", ont.getTxPower());
        metrics.put("distance", ont.getDistanceKm());
        response.setMetrics(metrics);

        String technology = "GPON";
        String rxStatus = getRxLevel(ont.getRxPower(), technology);
        String txStatus = evaluateTxPower(ont.getTxPower());
        double lossWarning = getLossDeviationWarning();

        List<AIDiagnosticResponse.Anomaly> anomalies = new ArrayList<>();

        if ("CRITICAL".equals(rxStatus)) {
            anomalies.add(new AIDiagnosticResponse.Anomaly(
                    "RX_CRITICAL",
                    "Puissance RX critique: " + ont.getRxPower() + " dBm",
                    90
            ));
        } else if ("DEGRADED".equals(rxStatus)) {
            anomalies.add(new AIDiagnosticResponse.Anomaly(
                    "RX_DEGRADED",
                    "Puissance RX dégradée: " + ont.getRxPower() + " dBm",
                    60
            ));
        }

        if ("CRITICAL".equals(txStatus)) {
            anomalies.add(new AIDiagnosticResponse.Anomaly(
                    "TX_CRITICAL",
                    "Puissance TX critique: " + ont.getTxPower() + " dBm",
                    90
            ));
        } else if ("DEGRADED".equals(txStatus)) {
            anomalies.add(new AIDiagnosticResponse.Anomaly(
                    "TX_DEGRADED",
                    "Puissance TX dégradée: " + ont.getTxPower() + " dBm",
                    60
            ));
        }

        if ("offline".equals(ont.getStatus())) {
            anomalies.add(new AIDiagnosticResponse.Anomaly(
                    "OFFLINE",
                    "ONT hors ligne",
                    80
            ));
        }

        if (ont.getSplitter() != null) {
            double lossDeviation = ExpertRules.calculateLossDeviation(
                    ont.getTxPower(),
                    ont.getRxPower(),
                    ont.getSplitter().getRatio(),
                    ont.getDistanceKm()
            );
            metrics.put("lossDeviation", Math.round(lossDeviation * 100) / 100.0);

            if (Math.abs(lossDeviation) > lossWarning) {
                anomalies.add(new AIDiagnosticResponse.Anomaly(
                        "LOSS_HIGH",
                        "Perte anormale: " + String.format("%.2f", lossDeviation) + " dB",
                        70
                ));
            }
        }

        response.setAnomalies(anomalies);

        List<String> recommendations = new ArrayList<>();
        for (AIDiagnosticResponse.Anomaly anomaly : anomalies) {
            recommendations.add(ExpertRules.getRecommendation(anomaly.getType(), anomaly.getSeverity()));
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Aucune anomalie détectée");
        }
        response.setRecommendations(recommendations);

        double confidence = anomalies.isEmpty() ? 95 : 85 - (anomalies.size() * 5);
        response.setConfidenceScore(Math.max(confidence, 50));

        Map<String, String> explanations = new HashMap<>();
        if (ont.getRxPower() < -25) {
            explanations.put("rxPower", "En dessous du seuil recommandé de -25 dBm");
        }
        if (ont.getTxPower() < 0.8) {
            explanations.put("txPower", "En dessous du seuil recommandé de 0.8 dBm");
        }
        response.setExplanations(explanations);

        if (request.isIncludeSimilarCases()) {
            List<AIDiagnosticResponse.SimilarCase> similarCases = findSimilarCases(ont);
            response.setSimilarCases(similarCases);
        }

        response.setStatus(ont.getStatus());

        return response;
    }

    private AIDiagnosticResponse diagnoseOLT(AIDiagnosticRequest request) {
        AIDiagnosticResponse response = new AIDiagnosticResponse();
        response.setResourceId(request.getResourceId());
        response.setResourceType("OLT");

        OLT olt = oltRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("OLT", "id", request.getResourceId()));

        Map<String, Double> metrics = new HashMap<>();
        metrics.put("totalPorts", (double) olt.getTotalPorts());
        metrics.put("utilizedPorts", (double) olt.getPons().size());
        metrics.put("utilizationRate", (olt.getPons().size() * 100.0) / olt.getTotalPorts());
        response.setMetrics(metrics);

        List<AIDiagnosticResponse.Anomaly> anomalies = new ArrayList<>();
        if (metrics.get("utilizationRate") > 90) {
            anomalies.add(new AIDiagnosticResponse.Anomaly(
                    "HIGH_UTILIZATION",
                    "Taux d'utilisation élevé: " + String.format("%.1f", metrics.get("utilizationRate")) + "%",
                    70
            ));
        }
        response.setAnomalies(anomalies);

        List<String> recommendations = new ArrayList<>();
        if (!anomalies.isEmpty()) {
            recommendations.add("Ajouter des ports ou planifier une extension");
        } else {
            recommendations.add("OLT fonctionne normalement");
        }
        response.setRecommendations(recommendations);

        response.setStatus("ACTIVE");
        response.setConfidenceScore(90);

        return response;
    }

    private AIDiagnosticResponse diagnoseSplitter(AIDiagnosticRequest request) {
        AIDiagnosticResponse response = new AIDiagnosticResponse();
        response.setResourceId(request.getResourceId());
        response.setResourceType("SPLITTER");

        Splitter splitter = splitterRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Splitter", "id", request.getResourceId()));

        Map<String, Double> metrics = new HashMap<>();
        metrics.put("lossDb", splitter.getLossDb());
        metrics.put("ratio", (double) splitter.getRatio());
        response.setMetrics(metrics);

        double theoreticalLoss = ExpertRules.SPLITTER_LOSS.getOrDefault(splitter.getRatio(), 15.0);
        double deviation = splitter.getLossDb() - theoreticalLoss;

        List<AIDiagnosticResponse.Anomaly> anomalies = new ArrayList<>();
        if (deviation > getLossDeviationWarning()) {
            anomalies.add(new AIDiagnosticResponse.Anomaly(
                    "HIGH_LOSS",
                    "Perte excessive: " + String.format("%.2f", splitter.getLossDb()) + " dB (théorique: " + theoreticalLoss + " dB)",
                    80
            ));
        }
        response.setAnomalies(anomalies);

        List<String> recommendations = new ArrayList<>();
        if (!anomalies.isEmpty()) {
            recommendations.add("Remplacer le splitter");
            recommendations.add("Vérifier les connecteurs");
        } else {
            recommendations.add("Splitter fonctionne normalement");
        }
        response.setRecommendations(recommendations);

        response.setStatus(anomalies.isEmpty() ? "GOOD" : "DEGRADED");
        response.setConfidenceScore(85);

        return response;
    }

    private AIDiagnosticResponse diagnosePON(AIDiagnosticRequest request) {
        AIDiagnosticResponse response = new AIDiagnosticResponse();
        response.setResourceId(request.getResourceId());
        response.setResourceType("PON");

        PON pon = ponRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("PON", "id", request.getResourceId()));

        List<Splitter> splitters = splitterRepository.findByParentPonId(pon.getId());
        int totalOnts = 0;
        int offlineOnts = 0;

        for (Splitter splitter : splitters) {
            List<ONT> onts = ontRepository.findBySplitterId(splitter.getId());
            totalOnts += onts.size();
            offlineOnts += onts.stream().filter(ont -> "offline".equals(ont.getStatus())).count();
        }

        Map<String, Double> metrics = new HashMap<>();
        metrics.put("txPower", pon.getTxPower());
        metrics.put("splitterCount", (double) splitters.size());
        metrics.put("totalOnts", (double) totalOnts);
        metrics.put("offlineRate", totalOnts > 0 ? (offlineOnts * 100.0 / totalOnts) : 0);
        response.setMetrics(metrics);

        List<AIDiagnosticResponse.Anomaly> anomalies = new ArrayList<>();
        if (metrics.get("offlineRate") > 20) {
            anomalies.add(new AIDiagnosticResponse.Anomaly(
                    "HIGH_OFFLINE_RATE",
                    "Taux d'ONTs hors ligne élevé: " + String.format("%.1f", metrics.get("offlineRate")) + "%",
                    75
            ));
        }
        response.setAnomalies(anomalies);

        response.setStatus("ACTIVE");
        response.setConfidenceScore(85);

        return response;
    }

    private List<AIDiagnosticResponse.SimilarCase> findSimilarCases(ONT ont) {
        List<AIDiagnosticResponse.SimilarCase> similarCases = new ArrayList<>();

        List<Incident> incidents = incidentRepository.findAll();

        for (Incident incident : incidents) {
            if ("ONT".equals(incident.getResource()) &&
                    Math.abs(incident.getValue() - ont.getRxPower()) < 5) {

                similarCases.add(new AIDiagnosticResponse.SimilarCase(
                        incident.getId(),
                        incident.getType() + " avec valeur " + incident.getValue(),
                        incident.getRecommendation(),
                        70 + (int)(Math.random() * 20)
                ));

                if (similarCases.size() >= 3) break;
            }
        }

        return similarCases;
    }
}