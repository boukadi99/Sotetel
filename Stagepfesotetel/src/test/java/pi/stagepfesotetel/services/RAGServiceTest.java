package pi.stagepfesotetel.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RAGServiceTest {
    private RAGService ragService;
    @Mock private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        ragService = new RAGService();
        ReflectionTestUtils.setField(ragService, "ragServiceUrl", "http://rag.example");
        ReflectionTestUtils.setField(ragService, "restTemplate", restTemplate);
    }

    @Test
    void askQuestion_success_returnsServiceBody() {
        Map<String, Object> expected = Map.of("answer", "Fiber optics is light transmission", "sources", new String[]{"guide"});
        when(restTemplate.postForEntity(eq("http://rag.example/chat"), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(expected, HttpStatus.OK));
        assertEquals(expected, ragService.askQuestion("What is fiber optics?"), "The RAG response body must be returned unchanged");
    }

    @Test
    void askQuestion_serviceUnavailable_returnsStableFailureMessage() {
        when(restTemplate.postForEntity(eq("http://rag.example/chat"), any(), eq(Map.class)))
                .thenThrow(new ResourceAccessException("connection refused"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> ragService.askQuestion("question"),
                "An unavailable RAG service must produce an application-level failure");
        assertEquals("Le service chatbot est temporairement indisponible.", exception.getMessage(),
                "The caller-facing fallback message must remain stable");
    }

    @Test
    void askQuestion_httpClientError_returnsStableFailureMessage() {
        when(restTemplate.postForEntity(eq("http://rag.example/chat"), any(), eq(Map.class)))
                .thenThrow(new org.springframework.web.client.HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assertThrows(RuntimeException.class, () -> ragService.askQuestion("bad request"),
                "HTTP client errors from RAG must be translated to a runtime failure");
    }
}
