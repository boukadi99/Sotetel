package pi.stagepfesotetel.dto;

import jakarta.validation.constraints.NotNull;

public class AIDiagnosticRequest {

    @NotNull(message = "L'ID de la ressource est obligatoire")
    private Long resourceId;

    @NotNull(message = "Le type de ressource est obligatoire")
    private String resourceType; // "ONT", "OLT", "SPLITTER", "PON"

    private boolean includeHistory = false;
    private boolean includeSimilarCases = false;

    // Getters et Setters
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public boolean isIncludeHistory() { return includeHistory; }
    public void setIncludeHistory(boolean includeHistory) { this.includeHistory = includeHistory; }

    public boolean isIncludeSimilarCases() { return includeSimilarCases; }
    public void setIncludeSimilarCases(boolean includeSimilarCases) { this.includeSimilarCases = includeSimilarCases; }
}