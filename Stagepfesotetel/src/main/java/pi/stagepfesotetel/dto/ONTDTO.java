package pi.stagepfesotetel.dto;

public class ONTDTO {
    private Long id;
    private String serial;
    private Double rxPower;
    private Double txPower;
    private Double distanceKm;
    private String status;
    private Long splitterId;  // Au lieu de tout l'objet Splitter
    private String healthStatus; // "GOOD", "WARNING", "CRITICAL"

    public ONTDTO() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public Double getRxPower() { return rxPower; }
    public void setRxPower(Double rxPower) { this.rxPower = rxPower; }

    public Double getTxPower() { return txPower; }
    public void setTxPower(Double txPower) { this.txPower = txPower; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getSplitterId() { return splitterId; }
    public void setSplitterId(Long splitterId) { this.splitterId = splitterId; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
}