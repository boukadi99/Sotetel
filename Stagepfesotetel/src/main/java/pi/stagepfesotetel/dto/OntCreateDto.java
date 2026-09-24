package pi.stagepfesotetel.dto;

import jakarta.validation.constraints.*;

public class OntCreateDto {

    @NotBlank(message = "Le serial est obligatoire")
    private String serial;

    @NotNull(message = "La puissance RX est obligatoire")
    private Double rxPower;

    @NotNull(message = "La puissance TX est obligatoire")
    private Double txPower;

    private Double distanceKm;

    @NotBlank(message = "Le statut est obligatoire")
    @Pattern(regexp = "online|offline|degraded", message = "Statut doit être online, offline ou degraded")
    private String status;

    @NotNull(message = "L'ID du splitter est obligatoire")
    private Long splitterId;

    // Getters et Setters
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
}