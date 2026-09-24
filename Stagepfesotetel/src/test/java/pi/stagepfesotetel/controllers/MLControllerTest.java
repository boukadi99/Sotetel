package pi.stagepfesotetel.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import pi.stagepfesotetel.entities.ONT;
import pi.stagepfesotetel.repositories.ONTRepository;
import pi.stagepfesotetel.services.AnomalyDetectionService;

import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MLControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private AnomalyDetectionService anomalyDetectionService;
    @MockBean private ONTRepository ontRepository;

    @Test
    void train_triggersModelTraining_returns200() throws Exception {
        doNothing().when(anomalyDetectionService).trainModel();
        mockMvc.perform(post("/api/ml/train"))
                .andExpect(status().isOk())
                .andExpect(content().string("Model training initiated."));
    }

    @Test
    void getAnomaly_existingOntWithModel_returnsScore() throws Exception {
        ONT ont = new ONT(); ont.setId(1L);
        when(ontRepository.findById(1L)).thenReturn(Optional.of(ont));
        when(anomalyDetectionService.scoreONT(ont)).thenReturn(Optional.of(new AnomalyDetectionService.AnomalyResult(0.25, "normal")));
        mockMvc.perform(get("/api/ml/anomaly/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(0.25))
                .andExpect(jsonPath("$.classification").value("normal"));
    }

    @Test
    void getAnomaly_missingOnt_returns400() throws Exception {
        when(ontRepository.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/ml/anomaly/99"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ONT not found with id: 99"));
    }
}
