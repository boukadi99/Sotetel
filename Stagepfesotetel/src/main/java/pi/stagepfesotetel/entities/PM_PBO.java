package pi.stagepfesotetel.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "pm_pbo")
public class PM_PBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pm_id")
    private Long id;

    private String type;
    private Integer capacity;
    private String location;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // Constructeurs
    public PM_PBO() {}

    public PM_PBO(String type, Integer capacity, String location, Double latitude, Double longitude) {
        this.type = type;
        this.capacity = capacity;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}