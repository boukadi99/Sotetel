package pi.stagepfesotetel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class StagepfesotetelApplicationTests {

    @Test
    void contextLoads() {
        assertNotNull(StagepfesotetelApplication.class, "The Spring Boot application class must be available");
    }

    @Test
    void applicationClass_canBeConstructed() {
        assertNotNull(new StagepfesotetelApplication(), "The application entry-point class must be constructible");
    }

}
