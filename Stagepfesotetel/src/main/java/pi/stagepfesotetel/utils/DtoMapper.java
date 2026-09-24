package pi.stagepfesotetel.utils;

import pi.stagepfesotetel.dto.*;
import pi.stagepfesotetel.entities.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    // ========== OLT MAPPING ==========

    public OLTDTO toDto(OLT olt) {
        if (olt == null) return null;
        return new OLTDTO(
                olt.getId(),
                olt.getSite(),
                olt.getVendor(),
                olt.getTotalPorts(),
                olt.getPons() != null ? olt.getPons().size() : 0
        );
    }

    public List<OLTDTO> toOltDtoList(List<OLT> olts) {
        return olts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ========== ONT MAPPING ==========

    public ONTDTO toDto(ONT ont) {
        if (ont == null) return null;
        ONTDTO dto = new ONTDTO();
        dto.setId(ont.getId());
        dto.setSerial(ont.getSerial());
        dto.setRxPower(ont.getRxPower());
        dto.setTxPower(ont.getTxPower());
        dto.setDistanceKm(ont.getDistanceKm());
        dto.setStatus(ont.getStatus());

        if (ont.getSplitter() != null) {
            dto.setSplitterId(ont.getSplitter().getId());
        }

        // Calcul du healthStatus
        if (ont.getRxPower() < -27) {
            dto.setHealthStatus("CRITICAL");
        } else if (ont.getRxPower() < -25) {
            dto.setHealthStatus("WARNING");
        } else {
            dto.setHealthStatus("GOOD");
        }

        return dto;
    }

    public List<ONTDTO> toOntDtoList(List<ONT> onts) {
        return onts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ========== PON MAPPING ==========

    public PONDTO toDto(PON pon) {
        if (pon == null) return null;
        PONDTO dto = new PONDTO();
        dto.setId(pon.getId());
        dto.setPortIndex(pon.getPortIndex());
        dto.setTxPower(pon.getTxPower());

        if (pon.getOlt() != null) {
            dto.setOltId(pon.getOlt().getId());
        }

        dto.setSplitterCount(pon.getSplitters() != null ? pon.getSplitters().size() : 0);

        return dto;
    }

    public List<PONDTO> toPonDtoList(List<PON> pons) {
        return pons.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ========== SPLITTER MAPPING ==========

    public SplitterDTO toDto(Splitter splitter) {
        if (splitter == null) return null;
        SplitterDTO dto = new SplitterDTO();
        dto.setId(splitter.getId());
        dto.setRatio(splitter.getRatio());
        dto.setLossDb(splitter.getLossDb());

        if (splitter.getParentPon() != null) {
            dto.setPonId(splitter.getParentPon().getId());
        }

        dto.setOntCount(splitter.getOnts() != null ? splitter.getOnts().size() : 0);

        return dto;
    }

    public List<SplitterDTO> toSplitterDtoList(List<Splitter> splitters) {
        return splitters.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ========== PM/PBO MAPPING ==========

    public PMPBODTO toDto(PM_PBO pmPbo) {
        if (pmPbo == null) return null;
        PMPBODTO dto = new PMPBODTO();
        dto.setId(pmPbo.getId());
        dto.setType(pmPbo.getType());
        dto.setCapacity(pmPbo.getCapacity());
        dto.setLocation(pmPbo.getLocation());
        dto.setLatitude(pmPbo.getLatitude());
        dto.setLongitude(pmPbo.getLongitude());
        return dto;
    }

    public List<PMPBODTO> toPmPboDtoList(List<PM_PBO> pmPboList) {
        return pmPboList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}