package pi.stagepfesotetel.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "thresholds")
public class Threshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;        // ex: "rx_critical_gpon", "tx_degraded"

    private String technology;  // "GPON", "XGS-PON", "DEFAULT"
    private Double value;
    private String unit;        // "dBm", "dB", "km"
    private String description;

    // Constructeurs
    public Threshold() {}

    public Threshold(String name, String technology, Double value, String unit, String description) {
        this.name = name;
        this.technology = technology;
        this.value = value;
        this.unit = unit;
        this.description = description;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTechnology() { return technology; }
    public void setTechnology(String technology) { this.technology = technology; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}