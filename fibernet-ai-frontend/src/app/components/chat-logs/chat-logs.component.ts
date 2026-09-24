import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { ChatLog } from '../../models/chat-log.model';

@Component({
  selector: 'app-chat-logs',
  templateUrl: './chat-logs.component.html',
  styleUrls: ['./chat-logs.component.css']
})
export class ChatLogsComponent implements OnInit {
  logs: ChatLog[] = [];
  filteredLogs: ChatLog[] = [];

  loading = false;
  error: string | null = null;

  sourceFilter = '';
  dateFilter = '';
  searchTerm = '';

  pageSize = 10;
  currentPage = 1;

  readonly sourceOptions = [
    'diagnostic_ont',
    'procedure_sfp',
    'procedure_ont',
    'security_hse',
    'welcome'
  ];

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.loading = true;
    this.error = null;

    this.apiService.getChatLogs().subscribe({
      next: (response) => {
        const rows = this.extractArray(response)
          .map((item) => this.normalizeLog(item))
          .filter((row) => row.id !== null);

        this.logs = rows;
        this.currentPage = 1;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement historique chatbot:', err);
        this.error = 'Impossible de charger les logs du chatbot.';
        this.logs = [];
        this.filteredLogs = [];
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    const term = this.searchTerm.trim().toLowerCase();

    this.filteredLogs = this.logs.filter((log) => {
      const sourceMatch = !this.sourceFilter || log.intent === this.sourceFilter;
      const dateMatch = !this.dateFilter || this.toDateKey(log.timestamp) === this.dateFilter;
      const textMatch = !term || log.question.toLowerCase().includes(term);
      return sourceMatch && dateMatch && textMatch;
    });

    this.currentPage = 1;
  }

  get paginatedLogs(): ChatLog[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredLogs.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredLogs.length / this.pageSize));
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, idx) => idx + 1);
  }

  setPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.currentPage = page;
  }

  prevPage(): void {
    this.setPage(this.currentPage - 1);
  }

  nextPage(): void {
    this.setPage(this.currentPage + 1);
  }

  clearFilters(): void {
    this.sourceFilter = '';
    this.dateFilter = '';
    this.searchTerm = '';
    this.applyFilters();
  }

  truncate(text: string, max = 100): string {
    if (!text) {
      return '';
    }
    return text.length > max ? `${text.slice(0, max)}...` : text;
  }

  getSourceBadgeClass(source: string): string {
    switch ((source || '').toLowerCase()) {
      case 'diagnostic_ont':
        return 'bg-danger';
      case 'procedure_sfp':
        return 'bg-info text-dark';
      case 'procedure_ont':
        return 'bg-warning text-dark';
      case 'security_hse':
        return 'bg-success';
      case 'welcome':
        return 'bg-secondary';
      default:
        return 'bg-dark';
    }
  }

  trackById(index: number, item: ChatLog): number {
    return item.id || index;
  }

  private extractArray(response: any): any[] {
    if (Array.isArray(response)) {
      return response;
    }
    if (Array.isArray(response?.content)) {
      return response.content;
    }
    if (Array.isArray(response?.data)) {
      return response.data;
    }
    if (Array.isArray(response?.items)) {
      return response.items;
    }
    return [];
  }

  private normalizeLog(raw: any): ChatLog {
    const id = Number(raw?.id ?? 0);
    const user = String(raw?.user ?? raw?.username ?? raw?.askedBy ?? raw?.createdBy ?? '').trim();
    const question = String(raw?.question ?? '').trim();
    const reponse = String(raw?.reponse ?? raw?.response ?? '').trim();
    const source = String(raw?.source ?? '').trim();
    const intent = String(raw?.intent ?? '').trim();
    const sessionId = String(raw?.sessionId ?? '').trim();
    const parsedDate = new Date(raw?.timestamp);

    return {
      id,
      user: user || '-',
      question,
      reponse,
      source,
      intent,
      sessionId,
      timestamp: Number.isNaN(parsedDate.getTime()) ? new Date() : parsedDate
    };
  }

  private toDateKey(value: Date): string {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return '';
    }
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

}
