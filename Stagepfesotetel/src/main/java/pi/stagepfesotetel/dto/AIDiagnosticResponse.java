package pi.stagepfesotetel.dto;

import java.util.List;
import java.util.Map;

public class AIDiagnosticResponse {

    private Long resourceId;
    private String resourceType;
    private String status;
    private Map<String, Double> metrics;
    private List<Anomaly> anomalies;
    private List<String> recommendations;
    private Map<String, String> explanations;
    private double confidenceScore;
    private List<SimilarCase> similarCases;

    // Classe interne pour les anomalies
    public static class Anomaly {
        private String type;
        private String description;
        private double severity; // 0-100
        private Map<String, Object> details;

        public Anomaly() {}

        public Anomaly(String type, String description, double severity) {
            this.type = type;
            this.description = description;
            this.severity = severity;
        }

        // Getters et Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getSeverity() { return severity; }
        public void setSeverity(double severity) { this.severity = severity; }

        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
    }

    // Classe interne pour les cas similaires
    public static class SimilarCase {
        private Long id;
        private String description;
        private String resolution;
        private double similarity;

        public SimilarCase() {}

        public SimilarCase(Long id, String description, String resolution, double similarity) {
            this.id = id;
            this.description = description;
            this.resolution = resolution;
            this.similarity = similarity;
        }

        // Getters et Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }

        public double getSimilarity() { return similarity; }
        public void setSimilarity(double similarity) { this.similarity = similarity; }
    }

    // Getters et Setters principaux
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Double> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Double> metrics) { this.metrics = metrics; }

    public List<Anomaly> getAnomalies() { return anomalies; }
    public void setAnomalies(List<Anomaly> anomalies) { this.anomalies = anomalies; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public Map<String, String> getExplanations() { return explanations; }
    public void setExplanations(Map<String, String> explanations) { this.explanations = explanations; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public List<SimilarCase> getSimilarCases() { return similarCases; }
    public void setSimilarCases(List<SimilarCase> similarCases) { this.similarCases = similarCases; }
}