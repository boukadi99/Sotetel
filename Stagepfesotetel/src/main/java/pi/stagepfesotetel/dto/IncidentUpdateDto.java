package pi.stagepfesotetel.dto;

public class IncidentUpdateDto {

    private String status;  // PENDING, IN_PROGRESS, RESOLVED
    private String recommendation;

    // Getters et Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}