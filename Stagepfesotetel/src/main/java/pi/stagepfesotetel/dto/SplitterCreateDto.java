package pi.stagepfesotetel.dto;

import jakarta.validation.constraints.*;

public class SplitterCreateDto {

    @NotNull(message = "Le ratio est obligatoire")
    @Min(value = 4, message = "Le ratio doit être 4, 8, 16 ou 32")
    @Max(value = 32, message = "Le ratio doit être 4, 8, 16 ou 32")
    private Integer ratio;

    private Double lossDb;

    @NotNull(message = "L'ID du PON est obligatoire")
    private Long ponId;

    // Getters et Setters
    public Integer getRatio() { return ratio; }
    public void setRatio(Integer ratio) { this.ratio = ratio; }

    public Double getLossDb() { return lossDb; }
    public void setLossDb(Double lossDb) { this.lossDb = lossDb; }

    public Long getPonId() { return ponId; }
    public void setPonId(Long ponId) { this.ponId = ponId; }
}