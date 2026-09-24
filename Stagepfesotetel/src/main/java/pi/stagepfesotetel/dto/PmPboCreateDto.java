package pi.stagepfesotetel.dto;

import jakarta.validation.constraints.*;

public class PmPboCreateDto {

    @NotBlank(message = "Le type est obligatoire")
    private String type;

    @NotNull(message = "La capacité est obligatoire")
    @Min(value = 1, message = "La capacité doit être positive")
    private Integer capacity;

    @NotBlank(message = "La localisation est obligatoire")
    private String location;

    private Double latitude;
    private Double longitude;

    // Getters et Setters
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