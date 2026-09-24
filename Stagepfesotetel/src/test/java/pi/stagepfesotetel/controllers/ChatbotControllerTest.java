package pi.stagepfesotetel.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pi.stagepfesotetel.repositories.ChatLogRepository;
import pi.stagepfesotetel.repositories.UserRepository;
import pi.stagepfesotetel.services.AIService;
import pi.stagepfesotetel.services.DialogflowService;
import pi.stagepfesotetel.services.RAGService;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChatbotControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private AIService aiService;
    @MockBean private DialogflowService dialogflowService;
    @MockBean private ChatLogRepository chatLogRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private RAGService ragService;

    @Test
    void query_validQuestion_returnsResponseText() throws Exception {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(ragService.askQuestion("What is fiber optics?")).thenReturn(Map.of("answer", "Fiber uses light", "sources", new String[]{"guide"}));
        mockMvc.perform(post("/api/chatbot/query").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"What is fiber optics?\",\"sessionId\":\"session123\",\"username\":\"testuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseText").value("Fiber uses light"));
    }

    @Test
    void query_emptyQuestion_isPassedToRagAndReturns200() throws Exception {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(ragService.askQuestion("")).thenReturn(Map.of("answer", "Please provide a valid question.", "sources", new String[]{}));
        mockMvc.perform(post("/api/chatbot/query").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"\",\"sessionId\":\"session123\",\"username\":\"testuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseText").value("Please provide a valid question."));
    }

    @Test
    void query_ragFailure_returnsFallbackWith200() throws Exception {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(ragService.askQuestion("What is fiber optics?")).thenThrow(new RuntimeException("down"));
        mockMvc.perform(post("/api/chatbot/query").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"What is fiber optics?\",\"sessionId\":\"session123\",\"username\":\"testuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseText").value("Le service chatbot est temporairement indisponible."));
    }
}
