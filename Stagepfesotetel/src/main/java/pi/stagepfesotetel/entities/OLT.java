package pi.stagepfesotetel.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "olts")
public class OLT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "olt_id")
    private Long id;

    private String site;
    private String vendor;

    @Column(name = "total_ports")
    private Integer totalPorts;

    @OneToMany(mappedBy = "olt")
    private List<PON> pons;

    // Constructors
    public OLT() {}

    public OLT(String site, String vendor, Integer totalPorts) {
        this.site = site;
        this.vendor = vendor;
        this.totalPorts = totalPorts;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public Integer getTotalPorts() { return totalPorts; }
    public void setTotalPorts(Integer totalPorts) { this.totalPorts = totalPorts; }

    public List<PON> getPons() { return pons; }
    public void setPons(List<PON> pons) { this.pons = pons; }
}