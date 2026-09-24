package pi.stagepfesotetel.services;

import pi.stagepfesotetel.entities.*;
import pi.stagepfesotetel.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class DataLoaderService {

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

    @PostConstruct
    @Transactional
    public void loadData() {
        System.out.println("===== LOADING CSV DATA =====");

        // Check if data already exists
        if (oltRepository.count() > 0) {
            System.out.println("Data already loaded, skipping...");
            return;
        }

        try {
            // 1. Load OLTs first
            loadOLTs();

            // 2. Load PONs (depends on OLTs)
            loadPONs();

            // 3. Load Splitters (depends on PONs)
            loadSplitters();

            // 4. Load ONTs (depends on Splitters)
            loadONTs();

            // 5. Load PM/PBO
            loadPMPBO();

            // 6. Load Incidents
            loadIncidents();

            System.out.println("===== DATA LOADED SUCCESSFULLY =====");
            System.out.println("OLTs: " + oltRepository.count());
            System.out.println("PONs: " + ponRepository.count());
            System.out.println("Splitters: " + splitterRepository.count());
            System.out.println("ONTs: " + ontRepository.count());
            System.out.println("PM/PBO: " + pmPboRepository.count());
            System.out.println("Incidents: " + incidentRepository.count());

        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadOLTs() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/data/OLT.csv")),
                StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    OLT olt = new OLT();
                    olt.setId(Long.parseLong(data[0]));
                    olt.setSite(data[1]);
                    olt.setVendor(data[2]);
                    olt.setTotalPorts(Integer.parseInt(data[3]));
                    oltRepository.save(olt);
                }
            }
        }
    }

    private void loadPONs() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/data/PON.csv")),
                StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    PON pon = new PON();
                    pon.setId(Long.parseLong(data[0]));

                    Long oltId = Long.parseLong(data[1]);
                    oltRepository.findById(oltId).ifPresent(pon::setOlt);

                    pon.setPortIndex(Integer.parseInt(data[2]));
                    pon.setTxPower(Double.parseDouble(data[3]));
                    ponRepository.save(pon);
                }
            }
        }
    }

    private void loadSplitters() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/data/Splitters.csv")),
                StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    Splitter splitter = new Splitter();
                    splitter.setId(Long.parseLong(data[0]));

                    Long ponId = Long.parseLong(data[1]);
                    ponRepository.findById(ponId).ifPresent(splitter::setParentPon);

                    splitter.setRatio(Integer.parseInt(data[2]));
                    splitter.setLossDb(Double.parseDouble(data[3]));
                    splitterRepository.save(splitter);
                }
            }
        }
    }

    private void loadONTs() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/data/ONT.csv")),
                StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) {
                    ONT ont = new ONT();
                    ont.setId(Long.parseLong(data[0]));

                    Long splitterId = Long.parseLong(data[1]);
                    splitterRepository.findById(splitterId).ifPresent(ont::setSplitter);

                    ont.setSerial(data[2]);
                    ont.setRxPower(Double.parseDouble(data[3]));
                    ont.setTxPower(Double.parseDouble(data[4]));
                    ont.setDistanceKm(Double.parseDouble(data[5]));
                    ont.setStatus(data[6]);
                    ontRepository.save(ont);
                }
            }
        }
    }

    private void loadPMPBO() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/data/PM_PBO.csv")),
                StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    PM_PBO pmPbo = new PM_PBO();
                    pmPbo.setId(Long.parseLong(data[0]));
                    pmPbo.setType(data[1]);
                    pmPbo.setCapacity(Integer.parseInt(data[2]));
                    pmPbo.setLocation(data[3]);
                    pmPboRepository.save(pmPbo);
                }
            }
        }
    }

    private void loadIncidents() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/data/Incidents.csv")),
                StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5) {
                    Incident incident = new Incident();
                    incident.setId(Long.parseLong(data[0]));
                    incident.setResource(data[1]);
                    incident.setValue(Double.parseDouble(data[2]));
                    incident.setType(data[3]);
                    incident.setRecommendation(data[4]);
                    incidentRepository.save(incident);
                }
            }
        }
    }
}