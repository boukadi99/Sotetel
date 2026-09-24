package pi.stagepfesotetel.dto;

public class MonitoringSummaryDTO {
    private Long ponId;
    private Integer portIndex;
    private Double txPower;
    private Long oltId;
    private String oltSite;

    // Statistiques
    private Integer splitterCount;
    private Integer ontCount;
    private Double averageRxPower;
    private Double averageTxPower;
    private Integer onlineOnts;
    private Integer offlineOnts;
    private Integer degradedOnts;

    // Métriques optiques
    private Double opticalBudget;
    private Double theoreticalLoss;
    private Double actualLoss;
    private Double lossDeviation;

    // Constructeurs
    public MonitoringSummaryDTO() {}

    // Getters et Setters
    public Long getPonId() { return ponId; }
    public void setPonId(Long ponId) { this.ponId = ponId; }

    public Integer getPortIndex() { return portIndex; }
    public void setPortIndex(Integer portIndex) { this.portIndex = portIndex; }

    public Double getTxPower() { return txPower; }
    public void setTxPower(Double txPower) { this.txPower = txPower; }

    public Long getOltId() { return oltId; }
    public void setOltId(Long oltId) { this.oltId = oltId; }

    public String getOltSite() { return oltSite; }
    public void setOltSite(String oltSite) { this.oltSite = oltSite; }

    public Integer getSplitterCount() { return splitterCount; }
    public void setSplitterCount(Integer splitterCount) { this.splitterCount = splitterCount; }

    public Integer getOntCount() { return ontCount; }
    public void setOntCount(Integer ontCount) { this.ontCount = ontCount; }

    public Double getAverageRxPower() { return averageRxPower; }
    public void setAverageRxPower(Double averageRxPower) { this.averageRxPower = averageRxPower; }

    public Double getAverageTxPower() { return averageTxPower; }
    public void setAverageTxPower(Double averageTxPower) { this.averageTxPower = averageTxPower; }

    public Integer getOnlineOnts() { return onlineOnts; }
    public void setOnlineOnts(Integer onlineOnts) { this.onlineOnts = onlineOnts; }

    public Integer getOfflineOnts() { return offlineOnts; }
    public void setOfflineOnts(Integer offlineOnts) { this.offlineOnts = offlineOnts; }

    public Integer getDegradedOnts() { return degradedOnts; }
    public void setDegradedOnts(Integer degradedOnts) { this.degradedOnts = degradedOnts; }

    public Double getOpticalBudget() { return opticalBudget; }
    public void setOpticalBudget(Double opticalBudget) { this.opticalBudget = opticalBudget; }

    public Double getTheoreticalLoss() { return theoreticalLoss; }
    public void setTheoreticalLoss(Double theoreticalLoss) { this.theoreticalLoss = theoreticalLoss; }

    public Double getActualLoss() { return actualLoss; }
    public void setActualLoss(Double actualLoss) { this.actualLoss = actualLoss; }

    public Double getLossDeviation() { return lossDeviation; }
    public void setLossDeviation(Double lossDeviation) { this.lossDeviation = lossDeviation; }
}