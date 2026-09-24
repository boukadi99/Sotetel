package pi.stagepfesotetel.dto;

import java.util.List;

public class OntFilterDto {
    private List<String> status;        // online, offline, degraded
    private Double minRxPower;
    private Double maxRxPower;
    private Double minTxPower;
    private Double maxTxPower;
    private Double minDistance;
    private Double maxDistance;
    private Long splitterId;
    private String searchSerial;        // recherche partielle

    // Constructeurs
    public OntFilterDto() {}

    // Getters et Setters
    public List<String> getStatus() { return status; }
    public void setStatus(List<String> status) { this.status = status; }

    public Double getMinRxPower() { return minRxPower; }
    public void setMinRxPower(Double minRxPower) { this.minRxPower = minRxPower; }

    public Double getMaxRxPower() { return maxRxPower; }
    public void setMaxRxPower(Double maxRxPower) { this.maxRxPower = maxRxPower; }

    public Double getMinTxPower() { return minTxPower; }
    public void setMinTxPower(Double minTxPower) { this.minTxPower = minTxPower; }

    public Double getMaxTxPower() { return maxTxPower; }
    public void setMaxTxPower(Double maxTxPower) { this.maxTxPower = maxTxPower; }

    public Double getMinDistance() { return minDistance; }
    public void setMinDistance(Double minDistance) { this.minDistance = minDistance; }

    public Double getMaxDistance() { return maxDistance; }
    public void setMaxDistance(Double maxDistance) { this.maxDistance = maxDistance; }

    public Long getSplitterId() { return splitterId; }
    public void setSplitterId(Long splitterId) { this.splitterId = splitterId; }

    public String getSearchSerial() { return searchSerial; }
    public void setSearchSerial(String searchSerial) { this.searchSerial = searchSerial; }
}