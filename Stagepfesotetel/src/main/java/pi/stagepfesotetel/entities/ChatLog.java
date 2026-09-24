package pi.stagepfesotetel.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_logs")
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String reponse;

    @Column(name = "source") // Renamed from "intent" to "source"
    private String source;

    private String sessionId;

    @Column(name = "username")  // ✅ ADD THIS FIELD
    private String username;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    // Constructeurs
    public ChatLog() {}

    public ChatLog(String question, String reponse, String source, String sessionId, String username) {
        this.question = question;
        this.reponse = reponse;
        this.source = source;  // SET SOURCE
        this.sessionId = sessionId;
        this.username = username;  // ✅ SET USERNAME
        this.timestamp = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }

    public String getSource() { return source; }  // GETTER FOR SOURCE
    public void setSource(String source) { this.source = source; }  // SETTER FOR SOURCE

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUsername() { return username; }  // ✅ GETTER
    public void setUsername(String username) { this.username = username; }  // ✅ SETTER

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}