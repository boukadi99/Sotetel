package pi.stagepfesotetel.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "onts")
public class ONT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ont_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "splitter_id")
    @JsonIgnore
    private Splitter splitter;

    private String serial;

    @Column(name = "rx_power")
    private Double rxPower;

    @Column(name = "tx_power")
    private Double txPower;

    @Column(name = "distance_km")
    private Double distanceKm;

    private String status;

    // Constructors
    public ONT() {}

    public ONT(Splitter splitter, String serial, Double rxPower, Double txPower, Double distanceKm, String status) {
        this.splitter = splitter;
        this.serial = serial;
        this.rxPower = rxPower;
        this.txPower = txPower;
        this.distanceKm = distanceKm;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Splitter getSplitter() { return splitter; }
    public void setSplitter(Splitter splitter) { this.splitter = splitter; }

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
}