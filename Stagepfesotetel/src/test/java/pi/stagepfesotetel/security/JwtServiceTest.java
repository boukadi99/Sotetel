package pi.stagepfesotetel.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "mysecretkeymysecretkeymysecretkeymysecretkey");
        ReflectionTestUtils.setField(jwtService, "expiration", 3_600_000L);
    }

    @Test
    void generateToken_validUser_returnsSignedToken() {
        String token = jwtService.generateToken("testuser", "ADMIN");
        assertNotNull(token, "A valid user must receive a JWT");
        assertFalse(token.isBlank(), "The generated JWT must not be blank");
    }

    @Test
    void extractClaims_validToken_returnsUsernameAndRole() {
        String token = jwtService.generateToken("testuser", "ADMIN");
        assertEquals("testuser", jwtService.extractUsername(token), "The JWT subject must be preserved");
        assertEquals("ADMIN", jwtService.extractRole(token), "The JWT role claim must be preserved");
        assertTrue(jwtService.validateToken(token), "A newly generated JWT must validate");
    }

    @Test
    void expiredToken_isRejected() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1_000L);
        String token = jwtService.generateToken("testuser", "ADMIN");
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token),
                "Reading an expired JWT must fail");
        assertFalse(jwtService.validateToken(token), "An expired JWT must not validate");
    }
}
