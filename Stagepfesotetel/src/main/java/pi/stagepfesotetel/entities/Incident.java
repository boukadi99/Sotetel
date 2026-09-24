package pi.stagepfesotetel.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_id")
    private Long id;

    private String resource;
    private Double value;
    private String type;
    private String recommendation;

    @Column(name = "status")
    private String status = "PENDING";

    // Constructeurs
    public Incident() {}

    public Incident(String resource, Double value, String type, String recommendation) {
        this.resource = resource;
        this.value = value;
        this.type = type;
        this.recommendation = recommendation;
        this.status = "PENDING";
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}