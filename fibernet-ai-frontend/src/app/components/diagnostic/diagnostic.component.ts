import { Component } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { AnomalyScoreResponse, MlService } from '../../services/ml.service';

@Component({
  selector: 'app-diagnostic',
  templateUrl: './diagnostic.component.html',
  styleUrls: ['./diagnostic.component.css']
})
export class DiagnosticComponent {
  // Formulaire
  ontId: number | null = null;
  resourceType: string = 'ONT';
  
  // États
  loading = false;
  result: any = null;
  error: string | null = null;
  mlResult: AnomalyScoreResponse | null = null;
  mlError: string | null = null;
  
  // Pour la sélection rapide
  quickSelectOptions = [1, 3, 8, 16, 360, 500];
  
  // Pour les calculs dans le template
  Math = Math;
  Object = Object;

  constructor(private apiService: ApiService, private mlService: MlService) { }

  diagnose(): void {
    if (!this.ontId) {
      this.error = 'Veuillez saisir un ID d\'ONT';
      return;
    }
    
    this.loading = true;
    this.error = null;
    this.result = null;
    this.mlResult = null;
    this.mlError = null;
    
    forkJoin({
      diagnostic: this.apiService.diagnoseOnt(this.ontId, true),
      anomaly: this.mlService.getAnomalyScore(this.ontId).pipe(
        catchError((err) => {
          this.mlError = this.getMlErrorMessage(err);
          return of(null);
        })
      )
    }).subscribe({
      next: ({ diagnostic, anomaly }) => {
        this.result = diagnostic;
        this.mlResult = anomaly;
        this.loading = false;
        console.log('Diagnostic:', diagnostic);
      },
      error: (err) => {
        console.error('Erreur:', err);
        this.error = 'Erreur lors du diagnostic. Vérifiez que l\'ID existe.';
        this.loading = false;
      }
    });
  }

  selectOnt(id: number): void {
    this.ontId = id;
    this.diagnose();
  }

  getSeverityClass(severity: number): string {
    if (severity >= 80) return 'bg-danger';
    if (severity >= 60) return 'bg-warning text-dark';
    return 'bg-info';
  }

  getSeverityText(severity: number): string {
    if (severity >= 80) return 'CRITIQUE';
    if (severity >= 60) return 'ÉLEVÉE';
    if (severity >= 40) return 'MOYENNE';
    return 'FAIBLE';
  }

  getHealthClass(healthStatus: string): string {
    switch (healthStatus) {
      case 'CRITICAL': return 'bg-danger';
      case 'WARNING': return 'bg-warning text-dark';
      case 'GOOD': return 'bg-success';
      default: return 'bg-secondary';
    }
  }

  getMlScore(): number {
    return Math.min(Math.max(this.mlResult?.score ?? 0, 0), 1);
  }

  getMlLabel(): string {
    const score = this.getMlScore();
    if (score < 0.4) return 'Normal';
    if (score < 0.7) return 'Suspect';
    return 'Anomalie';
  }

  getMlClass(): string {
    const score = this.getMlScore();
    if (score < 0.4) return 'ml-normal';
    if (score < 0.7) return 'ml-suspect';
    return 'ml-anomaly';
  }

  getMlExplanation(): string {
    switch (this.getMlLabel()) {
      case 'Suspect':
        return 'Comportement inhabituel détecté. Surveillance recommandée.';
      case 'Anomalie':
        return 'Anomalie détectée par l\'IA. Intervention recommandée.';
      default:
        return 'L\'ONT fonctionne normalement selon le modèle IA.';
    }
  }

  private getMlErrorMessage(err: any): string {
    const message = String(err?.error?.message || err?.message || '').toLowerCase();
    if (err?.status === 404 || message.includes('ont') && message.includes('not found')) {
      return 'ONT introuvable';
    }
    if (err?.status === 503 || message.includes('not trained') || message.includes('non entraîné')) {
      return 'Modèle IA non entraîné';
    }
    return 'Analyse IA indisponible';
  }
}