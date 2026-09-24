package pi.stagepfesotetel.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "pons")
public class PON {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pon_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "olt_id")
    @JsonIgnore
    private OLT olt;

    @Column(name = "port_index")
    private Integer portIndex;

    @Column(name = "tx_power")
    private Double txPower;

    @OneToMany(mappedBy = "parentPon")
    private List<Splitter> splitters;

    // Constructors
    public PON() {}

    public PON(OLT olt, Integer portIndex, Double txPower) {
        this.olt = olt;
        this.portIndex = portIndex;
        this.txPower = txPower;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OLT getOlt() {
        return olt;
    }

    public void setOlt(OLT olt) {
        this.olt = olt;
    }

    public Integer getPortIndex() {
        return portIndex;
    }

    public void setPortIndex(Integer portIndex) {
        this.portIndex = portIndex;
    }

    public Double getTxPower() {
        return txPower;
    }

    public void setTxPower(Double txPower) {
        this.txPower = txPower;
    }

    public List<Splitter> getSplitters() {
        return splitters;
    }

    public void setSplitters(List<Splitter> splitters) {
        this.splitters = splitters;
    }
}