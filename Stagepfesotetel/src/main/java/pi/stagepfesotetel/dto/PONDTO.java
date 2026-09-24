package pi.stagepfesotetel.dto;

public class PONDTO {
    private Long id;
    private Integer portIndex;
    private Double txPower;
    private Long oltId;
    private Integer splitterCount;

    public PONDTO() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPortIndex() { return portIndex; }
    public void setPortIndex(Integer portIndex) { this.portIndex = portIndex; }

    public Double getTxPower() { return txPower; }
    public void setTxPower(Double txPower) { this.txPower = txPower; }

    public Long getOltId() { return oltId; }
    public void setOltId(Long oltId) { this.oltId = oltId; }

    public Integer getSplitterCount() { return splitterCount; }
    public void setSplitterCount(Integer splitterCount) { this.splitterCount = splitterCount; }
}