package pi.stagepfesotetel.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class DiagnosticControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void diagnose_unauthenticatedRequest_returnsForbidden() throws Exception {
        Long ontId = 1L;

        mockMvc.perform(get("/api/diagnostic/ont/" + ontId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void diagnose_unknownOnt_unauthenticatedRequestStillReturnsForbidden() throws Exception {
        Long ontId = 99L;

        mockMvc.perform(get("/api/diagnostic/ont/" + ontId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
