package pi.stagepfesotetel.dto;

public class SplitterDTO {
    private Long id;
    private Integer ratio;
    private Double lossDb;
    private Long ponId;
    private Integer ontCount;

    public SplitterDTO() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getRatio() { return ratio; }
    public void setRatio(Integer ratio) { this.ratio = ratio; }

    public Double getLossDb() { return lossDb; }
    public void setLossDb(Double lossDb) { this.lossDb = lossDb; }

    public Long getPonId() { return ponId; }
    public void setPonId(Long ponId) { this.ponId = ponId; }

    public Integer getOntCount() { return ontCount; }
    public void setOntCount(Integer ontCount) { this.ontCount = ontCount; }
}