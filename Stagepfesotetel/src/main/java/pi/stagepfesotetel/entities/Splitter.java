package pi.stagepfesotetel.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "splitters")
public class Splitter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "splitter_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_pon")
    @JsonIgnore
    private PON parentPon;

    private Integer ratio;  // 4, 8, 16, 32

    @Column(name = "loss_db")
    private Double lossDb;

    @OneToMany(mappedBy = "splitter")
    private List<ONT> onts;

    // Constructors
    public Splitter() {}

    public Splitter(PON parentPon, Integer ratio, Double lossDb) {
        this.parentPon = parentPon;
        this.ratio = ratio;
        this.lossDb = lossDb;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PON getParentPon() { return parentPon; }
    public void setParentPon(PON parentPon) { this.parentPon = parentPon; }

    public Integer getRatio() { return ratio; }
    public void setRatio(Integer ratio) { this.ratio = ratio; }

    public Double getLossDb() { return lossDb; }
    public void setLossDb(Double lossDb) { this.lossDb = lossDb; }

    public List<ONT> getOnts() { return onts; }
    public void setOnts(List<ONT> onts) { this.onts = onts; }
}