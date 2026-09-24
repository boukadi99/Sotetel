package pi.stagepfesotetel.services;

import pi.stagepfesotetel.dto.MonitoringSummaryDTO;
import pi.stagepfesotetel.entities.*;
import pi.stagepfesotetel.exceptions.ResourceNotFoundException;
import pi.stagepfesotetel.repositories.*;
import pi.stagepfesotetel.utils.ThresholdCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonitoringService {

    @Autowired
    private OLTRepository oltRepository;

    @Autowired
    private PONRepository ponRepository;

    @Autowired
    private SplitterRepository splitterRepository;

    @Autowired
    private ONTRepository ontRepository;

    @Autowired
    private PM_PBORepository pmPboRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    // ========== 1. PON SUMMARY ==========

    public MonitoringSummaryDTO getPonSummary(Long ponId) {
        MonitoringSummaryDTO summary = new MonitoringSummaryDTO();

        // Récupérer le PON
        PON pon = ponRepository.findById(ponId)
                .orElseThrow(() -> new ResourceNotFoundException("PON", "id", ponId));

        // Informations de base
        summary.setPonId(pon.getId());
        summary.setPortIndex(pon.getPortIndex());
        summary.setTxPower(pon.getTxPower());

        if (pon.getOlt() != null) {
            summary.setOltId(pon.getOlt().getId());
            summary.setOltSite(pon.getOlt().getSite());
        }

        // Récupérer tous les splitters de ce PON
        List<Splitter> splitters = splitterRepository.findByParentPonId(ponId);
        summary.setSplitterCount(splitters.size());

        // Statistiques sur les ONTs
        int ontCount = 0;
        double totalRx = 0;
        double totalTx = 0;
        int onlineCount = 0;
        int offlineCount = 0;
        int degradedCount = 0;

        // Pour le calcul des pertes
        double totalActualLoss = 0;
        double totalTheoreticalLoss = 0;
        int lossCount = 0;

        for (Splitter splitter : splitters) {
            List<ONT> onts = ontRepository.findBySplitterId(splitter.getId());
            ontCount += onts.size();

            for (ONT ont : onts) {
                totalRx += ont.getRxPower();
                totalTx += ont.getTxPower();

                switch (ont.getStatus()) {
                    case "online": onlineCount++; break;
                    case "offline": offlineCount++; break;
                    case "degraded": degradedCount++; break;
                }

                // Calcul des pertes pour cet ONT
                if (ont.getRxPower() != null && ont.getTxPower() != null) {
                    double actualLoss = ThresholdCalculator.calculateOpticalBudget(
                            ont.getTxPower(), ont.getRxPower());
                    totalActualLoss += actualLoss;

                    double theoreticalLoss = ThresholdCalculator.calculateTheoreticalLoss(
                            splitter.getRatio(), ont.getDistanceKm());
                    totalTheoreticalLoss += theoreticalLoss;

                    lossCount++;
                }
            }
        }

        summary.setOntCount(ontCount);
        summary.setAverageRxPower(ontCount > 0 ?
                Math.round((totalRx / ontCount) * 100) / 100.0 : 0);
        summary.setAverageTxPower(ontCount > 0 ?
                Math.round((totalTx / ontCount) * 100) / 100.0 : 0);
        summary.setOnlineOnts(onlineCount);
        summary.setOfflineOnts(offlineCount);
        summary.setDegradedOnts(degradedCount);

        // Budget optique moyen
        if (ontCount > 0) {
            double avgBudget = summary.getAverageTxPower() - summary.getAverageRxPower();
            summary.setOpticalBudget(Math.round(avgBudget * 100) / 100.0);
        }

        // Pertes moyennes
        if (lossCount > 0) {
            double avgActualLoss = totalActualLoss / lossCount;
            double avgTheoreticalLoss = totalTheoreticalLoss / lossCount;
            summary.setActualLoss(Math.round(avgActualLoss * 100) / 100.0);
            summary.setTheoreticalLoss(Math.round(avgTheoreticalLoss * 100) / 100.0);
            summary.setLossDeviation(Math.round((avgActualLoss - avgTheoreticalLoss) * 100) / 100.0);
        }

        return summary;
    }

    // ========== 2. RÉSUMÉ GLOBAL DU RÉSEAU ==========

    public Map<String, Object> getNetworkSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Statistiques générales
        summary.put("totalOlts", oltRepository.count());
        summary.put("totalPons", ponRepository.count());
        summary.put("totalSplitters", splitterRepository.count());
        summary.put("totalOnts", ontRepository.count());
        summary.put("totalPmPbo", pmPboRepository.count());
        summary.put("totalIncidents", incidentRepository.count());

        // Statistiques ONT par statut
        Map<String, Long> ontStatus = new HashMap<>();
        ontStatus.put("online", ontRepository.countByStatus("online"));
        ontStatus.put("offline", ontRepository.countByStatus("offline"));
        ontStatus.put("degraded", ontRepository.countByStatus("degraded"));
        summary.put("ontStatus", ontStatus);

        // Pourcentage de santé
        long totalOnts = ontRepository.count();
        long onlineOnts = ontStatus.getOrDefault("online", 0L);
        double healthPercentage = totalOnts > 0 ?
                (onlineOnts * 100.0 / totalOnts) : 0;
        summary.put("healthPercentage", Math.round(healthPercentage * 10) / 10.0);

        // ONTs critiques
        summary.put("criticalOnts", ontRepository.findCriticalRxPower().size());

        return summary;
    }

    // ========== 3. RÉSUMÉ PAR OLT ==========

    public Map<String, Object> getOltSummary(Long oltId) {
        Map<String, Object> summary = new HashMap<>();

        OLT olt = oltRepository.findById(oltId)
                .orElseThrow(() -> new ResourceNotFoundException("OLT", "id", oltId));

        summary.put("oltId", olt.getId());
        summary.put("site", olt.getSite());
        summary.put("vendor", olt.getVendor());
        summary.put("totalPorts", olt.getTotalPorts());

        // Récupérer tous les PONs de cet OLT
        List<PON> pons = ponRepository.findByOltId(oltId);
        summary.put("ponCount", pons.size());

        // Compter tous les ONTs associés
        int totalOnts = 0;
        int onlineOnts = 0;
        int offlineOnts = 0;
        int degradedOnts = 0;
        double totalRx = 0;
        double totalTx = 0;

        for (PON pon : pons) {
            List<Splitter> splitters = splitterRepository.findByParentPonId(pon.getId());
            for (Splitter splitter : splitters) {
                List<ONT> onts = ontRepository.findBySplitterId(splitter.getId());
                totalOnts += onts.size();

                for (ONT ont : onts) {
                    totalRx += ont.getRxPower();
                    totalTx += ont.getTxPower();

                    switch (ont.getStatus()) {
                        case "online": onlineOnts++; break;
                        case "offline": offlineOnts++; break;
                        case "degraded": degradedOnts++; break;
                    }
                }
            }
        }

        summary.put("totalOnts", totalOnts);
        summary.put("onlineOnts", onlineOnts);
        summary.put("offlineOnts", offlineOnts);
        summary.put("degradedOnts", degradedOnts);

        if (totalOnts > 0) {
            summary.put("averageRxPower", Math.round((totalRx / totalOnts) * 100) / 100.0);
            summary.put("averageTxPower", Math.round((totalTx / totalOnts) * 100) / 100.0);
            summary.put("healthPercentage",
                    Math.round((onlineOnts * 100.0 / totalOnts) * 10) / 10.0);
        }

        return summary;
    }

    // ========== 4. RÉSUMÉ PAR SPLITTER ==========

    public Map<String, Object> getSplitterSummary(Long splitterId) {
        Map<String, Object> summary = new HashMap<>();

        Splitter splitter = splitterRepository.findById(splitterId)
                .orElseThrow(() -> new ResourceNotFoundException("Splitter", "id", splitterId));

        summary.put("splitterId", splitter.getId());
        summary.put("ratio", splitter.getRatio());
        summary.put("lossDb", splitter.getLossDb());

        if (splitter.getParentPon() != null) {
            summary.put("ponId", splitter.getParentPon().getId());
            if (splitter.getParentPon().getOlt() != null) {
                summary.put("oltId", splitter.getParentPon().getOlt().getId());
                summary.put("oltSite", splitter.getParentPon().getOlt().getSite());
            }
        }

        // Statistiques des ONTs sur ce splitter
        List<ONT> onts = ontRepository.findBySplitterId(splitterId);
        summary.put("ontCount", onts.size());

        int onlineOnts = 0;
        int offlineOnts = 0;
        int degradedOnts = 0;
        double totalRx = 0;
        double totalTx = 0;
        double totalActualLoss = 0;

        for (ONT ont : onts) {
            totalRx += ont.getRxPower();
            totalTx += ont.getTxPower();

            switch (ont.getStatus()) {
                case "online": onlineOnts++; break;
                case "offline": offlineOnts++; break;
                case "degraded": degradedOnts++; break;
            }

            if (ont.getRxPower() != null && ont.getTxPower() != null) {
                totalActualLoss += ThresholdCalculator.calculateOpticalBudget(
                        ont.getTxPower(), ont.getRxPower());
            }
        }

        summary.put("onlineOnts", onlineOnts);
        summary.put("offlineOnts", offlineOnts);
        summary.put("degradedOnts", degradedOnts);

        if (!onts.isEmpty()) {
            summary.put("averageRxPower", Math.round((totalRx / onts.size()) * 100) / 100.0);
            summary.put("averageTxPower", Math.round((totalTx / onts.size()) * 100) / 100.0);
            summary.put("averageActualLoss", Math.round((totalActualLoss / onts.size()) * 100) / 100.0);
            summary.put("theoreticalLoss", splitter.getLossDb());
            summary.put("lossDeviation",
                    Math.round((totalActualLoss / onts.size() - splitter.getLossDb()) * 100) / 100.0);
        }

        return summary;
    }

    public Map<String, Object> getPonTopology(Long ponId) {
        Map<String, Object> topology = new HashMap<>();

        PON pon = ponRepository.findById(ponId)
                .orElseThrow(() -> new ResourceNotFoundException("PON", "id", ponId));

        // Utiliser HashMap explicite au lieu de Map.of()
        Map<String, Object> ponInfo = new HashMap<>();
        ponInfo.put("id", pon.getId());
        ponInfo.put("portIndex", pon.getPortIndex());
        ponInfo.put("txPower", pon.getTxPower());
        topology.put("pon", ponInfo);

        if (pon.getOlt() != null) {
            Map<String, Object> oltInfo = new HashMap<>();
            oltInfo.put("id", pon.getOlt().getId());
            oltInfo.put("site", pon.getOlt().getSite());
            oltInfo.put("vendor", pon.getOlt().getVendor());
            topology.put("olt", oltInfo);
        }

        List<Map<String, Object>> splittersList = new ArrayList<>();
        List<Splitter> splitters = splitterRepository.findByParentPonId(ponId);

        for (Splitter splitter : splitters) {
            Map<String, Object> splitterInfo = new HashMap<>();
            splitterInfo.put("id", splitter.getId());
            splitterInfo.put("ratio", splitter.getRatio());
            splitterInfo.put("lossDb", splitter.getLossDb());

            List<Map<String, Object>> ontsList = new ArrayList<>();
            List<ONT> onts = ontRepository.findBySplitterId(splitter.getId());

            for (ONT ont : onts) {
                Map<String, Object> ontInfo = new HashMap<>();
                ontInfo.put("id", ont.getId());
                ontInfo.put("serial", ont.getSerial());
                ontInfo.put("rxPower", ont.getRxPower());
                ontInfo.put("status", ont.getStatus());
                ontInfo.put("distanceKm", ont.getDistanceKm());
                ontsList.add(ontInfo);
            }

            splitterInfo.put("onts", ontsList);
            splittersList.add(splitterInfo);
        }

        topology.put("splitters", splittersList);

        return topology;
    }

    // ========== 6. STATISTIQUES POUR DASHBOARD ==========

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> dashboard = new HashMap<>();

        long totalOnts = ontRepository.count();
        long onlineOnts = ontRepository.countByStatus("online");
        long offlineOnts = ontRepository.countByStatus("offline");
        long degradedOnts = ontRepository.countByStatus("degraded");
        long criticalOnts = ontRepository.findCriticalRxPower().size();

        dashboard.put("totalDevices",
                oltRepository.count() +
                        ponRepository.count() +
                        splitterRepository.count() +
                        totalOnts +
                        pmPboRepository.count()
        );

        dashboard.put("totalOlts", oltRepository.count());
        dashboard.put("totalPons", ponRepository.count());
        dashboard.put("totalSplitters", splitterRepository.count());
        dashboard.put("totalOnts", totalOnts);
        dashboard.put("totalPmPbo", pmPboRepository.count());
        dashboard.put("totalIncidents", incidentRepository.count());

        dashboard.put("onlineOnts", onlineOnts);
        dashboard.put("offlineOnts", offlineOnts);
        dashboard.put("degradedOnts", degradedOnts);
        dashboard.put("criticalOnts", criticalOnts);

        double availability = totalOnts > 0 ?
                (onlineOnts * 100.0 / totalOnts) : 0;
        dashboard.put("availability", Math.round(availability * 10) / 10.0);

        double failureRate = totalOnts > 0 ?
                ((offlineOnts + degradedOnts) * 100.0 / totalOnts) : 0;
        dashboard.put("failureRate", Math.round(failureRate * 10) / 10.0);

        return dashboard;
    }
}