import { Component } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface ChatMessage {
  role: 'user' | 'bot';
  content: string;
  timestamp: Date;
}

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent {
  private readonly sessionStorageKey = 'fibernet_ai_chatbot_session_id';
  private readonly currentUsername: string | null;

  messages: ChatMessage[] = [
    {
      role: 'bot',
      content: 'Bonjour. Je suis l\'assistant IA FiberNet-AI. Posez votre question technique.',
      timestamp: new Date()
    }
  ];

  quickSuggestions: string[] = [
    'Diagnostiquer un ONT',
    'Comment remplacer un SFP ?',
    'Consignes de sécurité'
  ];

  userInput = '';
  loading = false;
  error: string | null = null;
  private sessionId: string | null = null;

  constructor(private apiService: ApiService, private authService: AuthService) {
    this.sessionId = localStorage.getItem(this.sessionStorageKey);
    this.currentUsername = this.authService.getUsername();
  }

  sendMessage(input?: string): void {
    if (this.loading) {
      return;
    }

    const message = (input ?? this.userInput).trim();
    if (!message) {
      return;
    }

    this.messages.push({
      role: 'user',
      content: message,
      timestamp: new Date()
    });

    this.userInput = '';
    this.error = null;
    this.loading = true;

    this.apiService.queryChatbot(message, this.sessionId || undefined, this.currentUsername).subscribe({
      next: (response) => {
        console.log('FULL RESPONSE:', response);

        const botText = this.extractBotReply(response);

        this.messages.push({
          role: 'bot',
          content: botText,
          timestamp: new Date()
        });

        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chatbot:', err);
        this.error = 'Impossible de contacter l\'assistant IA. Veuillez réessayer.';
        this.loading = false;
      }
    });
  }

  onEnter(event: Event): void {
    event.preventDefault();
    this.sendMessage();
  }

  useSuggestion(suggestion: string): void {
    this.sendMessage(suggestion);
  }

  trackByIndex(index: number): number {
    return index;
  }

  private extractBotReply(response: any): string {
    const normalizedResponse = this.unwrapResponsePayload(response);

    if (typeof normalizedResponse === 'string') {
      return normalizedResponse;
    }

    if (!normalizedResponse || typeof normalizedResponse !== 'object') {
      return 'Réponse vide du chatbot.';
    }

    // ✅ RAG response field (from Spring Boot RAGService)
    if (normalizedResponse.responseText && typeof normalizedResponse.responseText === 'string' && normalizedResponse.responseText.trim().length > 0) {
      return normalizedResponse.responseText.trim();
    }

    // Legacy Dialogflow field
    if (normalizedResponse.reply && typeof normalizedResponse.reply === 'string' && normalizedResponse.reply.trim().length > 0) {
      return normalizedResponse.reply.trim();
    }

    // Other common fields
    const possibleFields = [
      normalizedResponse.fulfillmentText,
      normalizedResponse.response,
      normalizedResponse.answer,
      normalizedResponse.message,
      normalizedResponse.output,
      normalizedResponse.text,
      normalizedResponse.content
    ];

    for (const field of possibleFields) {
      if (typeof field === 'string' && field.trim().length > 0) {
        return field.trim();
      }
    }

    // Unwrap nested response objects
    const wrapperFields = ['body', 'data', 'result', 'payload'];
    for (const wrapperField of wrapperFields) {
      if (normalizedResponse[wrapperField]) {
        const nestedResponse = this.extractBotReply(normalizedResponse[wrapperField]);
        if (nestedResponse !== 'Je n\'ai pas pu interpréter la réponse du serveur.') {
          return nestedResponse;
        }
      }
    }

    return 'Je n\'ai pas pu interpréter la réponse du serveur.';
  }

  private unwrapResponsePayload(response: any): any {
    if (typeof response !== 'string') {
      return response;
    }

    const trimmedResponse = response.trim();
    if (!trimmedResponse) {
      return response;
    }

    if (!trimmedResponse.startsWith('{') && !trimmedResponse.startsWith('[')) {
      return response;
    }

    try {
      return JSON.parse(trimmedResponse);
    } catch {
      return response;
    }
  }
}