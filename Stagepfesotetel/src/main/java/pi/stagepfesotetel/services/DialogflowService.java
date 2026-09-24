package pi.stagepfesotetel.services;

import com.google.cloud.dialogflow.v2.*;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class DialogflowService {

    private static final String PROJECT_ID = "chatbot-pjgr";
    private static final String LANGUAGE_CODE = "fr";
    private SessionsClient sessionsClient;

    public DialogflowService() throws IOException {
        System.out.println("🔧 Initialisation DialogflowService...");

        // Charger les credentials
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ClassPathResource("credentials.json").getInputStream()
        );
        System.out.println("✅ Credentials chargés avec succès");

        SessionsSettings settings = SessionsSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        this.sessionsClient = SessionsClient.create(settings);
        System.out.println("✅ SessionsClient créé avec succès");
    }

    public String detectIntent(String text, String sessionId) {
        try {
            System.out.println("🔍 Appel Dialogflow - Texte: " + text);
            System.out.println("🔍 SessionId: " + sessionId);
            System.out.println("🔍 Project ID utilisé: " + PROJECT_ID);

            SessionName session = SessionName.of(PROJECT_ID, sessionId);
            TextInput.Builder textInput = TextInput.newBuilder()
                    .setText(text)
                    .setLanguageCode(LANGUAGE_CODE);
            QueryInput queryInput = QueryInput.newBuilder()
                    .setText(textInput)
                    .build();

            DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);
            QueryResult queryResult = response.getQueryResult();

            System.out.println("✅ Intent détecté: " + queryResult.getIntent().getDisplayName());

            return queryResult.getFulfillmentText();
        } catch (Exception e) {
            System.err.println("❌ Erreur Dialogflow: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}