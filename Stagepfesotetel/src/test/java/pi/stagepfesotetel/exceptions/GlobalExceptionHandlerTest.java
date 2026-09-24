package pi.stagepfesotetel.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    @Mock private WebRequest request;

    @Test
    void testHandleResourceNotFound_Returns404() {
        when(request.getDescription(false)).thenReturn("uri=/api/onts/42");
        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(
                new ResourceNotFoundException("ONT", "id", 42L), request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Missing resources must return HTTP 404");
        assertEquals("Not Found", response.getBody().get("error"), "The error label must describe a missing resource");
        assertEquals("/api/onts/42", response.getBody().get("path"), "The request path must be returned");
        assertNotNull(response.getBody().get("timestamp"), "The response must include a timestamp");
    }

    @Test
    void testHandleGenericException_Returns500() {
        when(request.getDescription(false)).thenReturn("uri=/api/failure");
        ResponseEntity<Map<String, Object>> response = handler.handleGlobalException(
                new RuntimeException("database down"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Unexpected failures must return HTTP 500");
        assertEquals("Une erreur inattendue s'est produite", response.getBody().get("message"),
                "The generic client message must not expose implementation details");
        assertEquals("database down", response.getBody().get("details"), "The exception detail must be retained");
    }
}
