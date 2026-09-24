package pi.stagepfesotetel.dto;

public class OLTDTO {
    private Long id;
    private String site;
    private String vendor;
    private Integer totalPorts;
    private Integer ponCount;  // Nombre de PONs associés (utile pour le frontend)

    public OLTDTO() {}

    public OLTDTO(Long id, String site, String vendor, Integer totalPorts, Integer ponCount) {
        this.id = id;
        this.site = site;
        this.vendor = vendor;
        this.totalPorts = totalPorts;
        this.ponCount = ponCount;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public Integer getTotalPorts() { return totalPorts; }
    public void setTotalPorts(Integer totalPorts) { this.totalPorts = totalPorts; }

    public Integer getPonCount() { return ponCount; }
    public void setPonCount(Integer ponCount) { this.ponCount = ponCount; }
}