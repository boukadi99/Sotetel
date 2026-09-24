import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface AnomalyScoreResponse {
  score: number;
  classification: string;
  anomaly: boolean;
  suspicious: boolean;
}

interface AnomalyApiResponse {
  score: number;
  classification: string;
  anomaly: boolean;
  suspicious?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class MlService {
  private readonly baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  getAnomalyScore(ontId: number): Observable<AnomalyScoreResponse> {
    return this.http
      .get<AnomalyApiResponse>(`${this.baseUrl}/ml/anomaly/${ontId}`)
      .pipe(
        map((response) => ({
          ...response,
          suspicious: response.suspicious ?? (response.score >= 0.4 && response.score < 0.7)
        }))
      );
  }
}