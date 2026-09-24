package pi.stagepfesotetel.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @InjectMocks private EmailService emailService;
    @Mock private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@fibernet-ai.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");
    }

    @Test
    void sendResetPasswordEmail_validData_sendsExpectedMessage() {
        emailService.sendResetPasswordEmail("test@example.com", "reset-token");
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("noreply@fibernet-ai.com", message.getFrom(), "The configured sender must be used");
        assertEquals("test@example.com", message.getTo()[0], "The reset email must target the requested address");
        assertTrue(message.getText().contains("http://localhost:4200/reset-password?token=reset-token"),
                "The email body must contain the reset URL");
    }

    @Test
    void sendResetPasswordEmail_mailSenderFails_propagatesFailure() {
        doThrow(new IllegalStateException("SMTP unavailable")).when(mailSender).send(any(SimpleMailMessage.class));
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> emailService.sendResetPasswordEmail("test@example.com", "reset-token"),
                "Mail transport failures must be visible to the caller");
        assertEquals("SMTP unavailable", exception.getMessage(), "The transport failure message must be retained");
    }

    @Test
    void sendResetPasswordEmail_differentToken_placesTokenInLink() {
        emailService.sendResetPasswordEmail("other@example.com", "another-token");
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(captor.getValue().getText().contains("token=another-token"),
                "Each reset email must include its own token");
    }
}
