package pi.stagepfesotetel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IncidentCreateDto {

    @NotBlank(message = "La ressource est obligatoire")
    private String resource;

    @NotNull(message = "La valeur est obligatoire")
    private Double value;

    @NotBlank(message = "Le type est obligatoire")
    private String type;

    private String recommendation;

    // Getters et Setters
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}