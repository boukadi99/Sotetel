package pi.stagepfesotetel.controllers;

import pi.stagepfesotetel.services.AIService;
import pi.stagepfesotetel.services.DialogflowService;
import pi.stagepfesotetel.services.RAGService;
import pi.stagepfesotetel.repositories.ChatLogRepository;
import pi.stagepfesotetel.entities.ChatLog;
import pi.stagepfesotetel.repositories.UserRepository;
import pi.stagepfesotetel.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private AIService aiService;

    @Autowired
    private DialogflowService dialogflowService;

    @Autowired
    private ChatLogRepository chatLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RAGService ragService;

    @PostMapping("/query")
    public Map<String, Object> handleQuery(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String sessionId = request.get("sessionId");
        String username = request.get("username");

        // ✅ FIX: Generate a session ID if it's null or empty
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = "session-" + System.currentTimeMillis() + "-" + Math.random();
        }

        System.out.println("=== REQUETE RECUE ===");
        System.out.println("Texte: " + text);
        System.out.println("SessionId: " + sessionId);
        System.out.println("Username: " + username);

        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(username + "@example.com");
            user.setPassword("defaultPassword");
            user.setRole("USER");
            userRepository.save(user);
        }

        try {
            Map<String, Object> ragResponse = ragService.askQuestion(text);
            Map<String, Object> response = new HashMap<>();
            response.put("responseText", ragResponse.get("answer"));
            response.put("sources", ragResponse.get("sources"));

            // Log the chat interaction
            ChatLog chatLog = new ChatLog(text, (String) ragResponse.get("answer"), "rag", sessionId, username);
            chatLogRepository.save(chatLog);

            return response;
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseText", "Le service chatbot est temporairement indisponible.");
            return errorResponse;
        }
    }

    // ========== ENDPOINT WEBHOOK POUR DIALOGFLOW ==========
    @PostMapping("/dialogflow-webhook")
    public Map<String, Object> dialogflowWebhook(@RequestBody Map<String, Object> request) {
        System.out.println("=== WEBHOOK RECEIVED ===");
        System.out.println("Timestamp: " + LocalDateTime.now());

        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> queryResult = (Map<String, Object>) request.get("queryResult");
            Map<String, Object> intent = (Map<String, Object>) queryResult.get("intent");
            String intentName = (String) intent.get("displayName");
            Map<String, Object> parameters = (Map<String, Object>) queryResult.get("parameters");

            System.out.println("Intent name: " + intentName);
            System.out.println("Parameters: " + parameters);

            // ========== 1. DIAGNOSTIC ONT ==========
            if ("diagnostic.ont".equals(intentName)) {
                System.out.println("✅ Intent diagnostic.ont détecté !");

                Object ontIdObj = parameters.get("ontId");
                Long ontId = null;

                if (ontIdObj instanceof Double) {
                    ontId = ((Double) ontIdObj).longValue();
                } else if (ontIdObj instanceof String) {
                    try {
                        ontId = Long.parseLong((String) ontIdObj);
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Erreur parsing String: " + e.getMessage());
                    }
                }

                // Fallback: extract from queryText
                if (ontId == null) {
                    String queryText = (String) queryResult.get("queryText");
                    if (queryText != null) {
                        Matcher matcher = Pattern.compile("\\d+").matcher(queryText);
                        if (matcher.find()) {
                            ontId = Long.parseLong(matcher.group());
                            System.out.println("ontId extrait depuis queryText: " + ontId);
                        }
                    }
                }

                if (ontId != null) {
                    Map<String, Object> diagnostic = aiService.diagnoseONT(ontId);
                    String reply = buildDiagnosticReply(ontId, diagnostic);
                    response.put("fulfillmentText", reply);
                } else {
                    response.put("fulfillmentText", "Veuillez préciser l'ID de l'ONT à diagnostiquer.");
                }
            }
            // ========== 2. PROCEDURE ONT ==========
            else if ("procedure_ont".equals(intentName) || "procedure_remplacement_ont".equals(intentName)) {
                System.out.println("✅ Intent procedure_ont détecté !");

                String reply = "🛠️ **Procédure de remplacement d'un ONT :**\n\n"
                        + "1. Débrancher l'alimentation électrique\n"
                        + "2. Débrancher la fibre optique\n"
                        + "3. Retirer l'ancien ONT\n"
                        + "4. Installer le nouveau ONT\n"
                        + "5. Rebrancher la fibre optique\n"
                        + "6. Rebrancher l'alimentation électrique\n"
                        + "7. Vérifier le signal (RX/TX)\n"
                        + "8. Confirmer le bon fonctionnement";

                response.put("fulfillmentText", reply);
            }
            // ========== 3. PROCEDURE SFP ==========
            else if ("procedure_sfp".equals(intentName) || "procedure_remplacement_sfp".equals(intentName)) {
                System.out.println("✅ Intent procedure_sfp détecté !");

                String reply = "🛠️ **Procédure de remplacement d'un SFP :**\n\n"
                        + "1. Identifier le port défectueux\n"
                        + "2. Débrancher le câble fibre\n"
                        + "3. Retirer l'ancien SFP\n"
                        + "4. Insérer le nouveau SFP\n"
                        + "5. Rebrancher le câble fibre\n"
                        + "6. Vérifier la liaison optique";

                response.put("fulfillmentText", reply);
            }
            // ========== 4. SECURITE HSE ==========
            else if ("security_hse".equals(intentName) || "consignes_securite".equals(intentName)) {
                System.out.println("✅ Intent security_hse détecté !");

                String reply = "🦺 **Consignes de sécurité HSE :**\n\n"
                        + "• Porter des EPI (casque, gants, lunettes)\n"
                        + "• Ne jamais regarder directement la fibre optique\n"
                        + "• Travailler en équipe lors des interventions\n"
                        + "• Couper l'alimentation avant toute manipulation\n"
                        + "• Vérifier les zones de danger\n"
                        + "• Respecter les distances de sécurité\n\n"
                        + "En cas de doute, contactez le responsable HSE.";

                response.put("fulfillmentText", reply);
            }
            // ========== 5. FALLBACK ==========
            else {
                System.out.println("⚠️ Intent non reconnue: " + intentName);
                response.put("fulfillmentText", "Je n'ai pas compris. Pouvez-vous reformuler ?");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur webhook: " + e.getMessage());
            e.printStackTrace();
            response.put("fulfillmentText", "Une erreur technique s'est produite.");
        }

        return response;
    }

    // ========== CONSULTER L'HISTORIQUE ==========
    @GetMapping("/logs")
    public List<ChatLog> getLogs() {
        return chatLogRepository.findAllByOrderByTimestampDesc();
    }

    @GetMapping("/intents")
    public Map<String, Object> getIntents() {
        return Map.of(
                "intents", List.of(
                        "diagnostic_ont", "procedure_sfp", "procedure_ont", "security_hse"
                ),
                "version", "1.0"
        );
    }

    // ========== MÉTHODE UTILITAIRE ==========
    private String buildDiagnosticReply(Long ontId, Map<String, Object> diagnostic) {
        StringBuilder reply = new StringBuilder();
        reply.append("🔍 **Diagnostic ONT #").append(ontId).append("**\n\n");
        reply.append("📊 **Métriques optiques :**\n");
        reply.append("• Puissance RX : ").append(diagnostic.get("rxPower")).append(" dBm\n");
        reply.append("• Puissance TX : ").append(diagnostic.get("txPower")).append(" dBm\n");
        reply.append("• Distance : ").append(diagnostic.get("distanceKm")).append(" km\n");
        reply.append("• Statut : ").append(diagnostic.get("status")).append("\n\n");

        @SuppressWarnings("unchecked")
        List<String> anomalies = (List<String>) diagnostic.get("anomalies");
        @SuppressWarnings("unchecked")
        List<String> recommendations = (List<String>) diagnostic.get("recommendations");

        if (anomalies != null && !anomalies.isEmpty()) {
            reply.append("⚠️ **Anomalies détectées :**\n");
            for (String anomaly : anomalies) {
                reply.append("• ").append(anomaly).append("\n");
            }
            reply.append("\n💡 **Recommandations :**\n");
            for (String rec : recommendations) {
                reply.append("• ").append(rec).append("\n");
            }
        } else {
            reply.append("✅ **Aucune anomalie détectée**\nL'ONT fonctionne normalement.");
        }

        return reply.toString();
    }
}