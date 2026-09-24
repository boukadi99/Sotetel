export interface ChatLog {
  id: number;
  user: string;
  question: string;
  reponse: string;
  source: string;
  intent: string;
  sessionId: string;
  timestamp: Date;
}
