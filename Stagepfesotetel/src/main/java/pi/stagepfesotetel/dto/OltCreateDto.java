package pi.stagepfesotetel.dto;

import jakarta.validation.constraints.*;

public class OltCreateDto {

    @NotBlank(message = "Le site est obligatoire")
    @Size(min = 2, max = 100, message = "Le site doit faire entre 2 et 100 caractères")
    private String site;

    @NotBlank(message = "Le vendor est obligatoire")
    private String vendor;

    @NotNull(message = "Le nombre de ports est obligatoire")
    @Min(value = 1, message = "Le nombre de ports doit être au moins 1")
    @Max(value = 100, message = "Le nombre de ports ne peut pas dépasser 100")
    private Integer totalPorts;

    // Getters et Setters
    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public Integer getTotalPorts() { return totalPorts; }
    public void setTotalPorts(Integer totalPorts) { this.totalPorts = totalPorts; }
}