package pi.stagepfesotetel.dto;

import jakarta.validation.constraints.*;

public class PonCreateDto {

    @NotNull(message = "L'index du port est obligatoire")
    @Min(value = 1, message = "L'index du port doit être au moins 1")
    private Integer portIndex;

    @NotNull(message = "La puissance TX est obligatoire")
    private Double txPower;

    @NotNull(message = "L'ID de l'OLT est obligatoire")
    private Long oltId;

    // Getters et Setters
    public Integer getPortIndex() { return portIndex; }
    public void setPortIndex(Integer portIndex) { this.portIndex = portIndex; }

    public Double getTxPower() { return txPower; }
    public void setTxPower(Double txPower) { this.txPower = txPower; }

    public Long getOltId() { return oltId; }
    public void setOltId(Long oltId) { this.oltId = oltId; }
}