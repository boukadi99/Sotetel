package pi.stagepfesotetel.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class RAGService {

    private static final Logger logger = Logger.getLogger(RAGService.class.getName());

    @Value("${rag.service.url:http://localhost:8000}")
    private String ragServiceUrl;

    private final RestTemplate restTemplate;

    public RAGService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> askQuestion(String question) {
        String endpoint = ragServiceUrl + "/chat";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("question", question);

        try {
            long startTime = System.currentTimeMillis();
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, requestBody, Map.class);
            long endTime = System.currentTimeMillis();

            logger.info("RAG response time: " + (endTime - startTime) + " ms");
            return response.getBody();
        } catch (HttpClientErrorException | ResourceAccessException e) {
            logger.severe("RAG service unavailable: " + e.getMessage());
            throw new RuntimeException("Le service chatbot est temporairement indisponible.");
        }
    }
}
