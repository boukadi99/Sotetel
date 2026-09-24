import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Threshold } from '../models/threshold.model';
import { ChatLog } from '../models/chat-log.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) { }

  // ========== DASHBOARD ==========

  getDashboardStats(): Observable<any> {
    return this.http.get(`${this.baseUrl}/monitoring/dashboard`);
  }

  getNetworkSummary(): Observable<any> {
    return this.http.get(`${this.baseUrl}/monitoring/network/summary`);
  }

  // ========== OLT ==========

  getOlts(page: number = 0, size: number = 20, sortBy: string = 'id', direction: string = 'asc'): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get(`${this.baseUrl}/inventory/olts/paginated`, { params });
  }

  getOltById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/olts/${id}`);
  }

  createOlt(olt: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/inventory/olts`, olt);
  }

  updateOlt(id: number, olt: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/inventory/olts/${id}`, olt);
  }

  deleteOlt(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/inventory/olts/${id}`);
  }

  // ========== PON ==========

  getPons(): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/pons`);
  }

  getPonsPaginated(page: number = 0, size: number = 20, sortBy: string = 'id', direction: string = 'asc'): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get(`${this.baseUrl}/inventory/pons/paginated`, { params });
  }

  getPonsByOltId(oltId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/pons/olt/${oltId}`);
  }

  createPon(pon: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/inventory/pons`, pon);
  }

  updatePon(id: number, pon: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/inventory/pons/${id}`, pon);
  }

  deletePon(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/inventory/pons/${id}`);
  }

  // ========== SPLITTER ==========

  getSplitters(): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/splitters`);
  }

  getSplittersPaginated(page: number = 0, size: number = 20, sortBy: string = 'id', direction: string = 'asc'): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get(`${this.baseUrl}/inventory/splitters/paginated`, { params });
  }

  getSplittersByPonId(ponId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/splitters/pon/${ponId}`);
  }

  createSplitter(splitter: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/inventory/splitters`, splitter);
  }

  updateSplitter(id: number, splitter: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/inventory/splitters/${id}`, splitter);
  }

  deleteSplitter(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/inventory/splitters/${id}`);
  }

  // ========== ONT ==========

  getOnts(page: number = 0, size: number = 20, sortBy: string = 'id', direction: string = 'asc'): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get(`${this.baseUrl}/inventory/onts/paginated`, { params });
  }

  getOntById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/onts/${id}`);
  }

  getOntsByStatus(status: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/onts/status/${status}`);
  }

  getCriticalOnts(): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/onts/critical`);
  }

  filterOnts(
    filters: any,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'id',
    direction: string = 'asc'
  ): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.post(`${this.baseUrl}/inventory/onts/filter`, filters, { params });
  }

  createOnt(ont: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/inventory/onts`, ont);
  }

  updateOnt(id: number, ont: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/inventory/onts/${id}`, ont);
  }

  deleteOnt(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/inventory/onts/${id}`);
  }

  // ========== PM/PBO ==========

  getPmPbo(): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/pm-pbo`);
  }

  getPmPboByType(type: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/pm-pbo/type/${type}`);
  }

  getPmPboPaginated(page: number = 0, size: number = 20, sortBy: string = 'id', direction: string = 'asc'): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get(`${this.baseUrl}/inventory/pm-pbo/paginated`, { params });
  }

  createPmPbo(pmPbo: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/inventory/pm-pbo`, pmPbo);
  }

  updatePmPbo(id: number, pmPbo: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/inventory/pm-pbo/${id}`, pmPbo);
  }

  deletePmPbo(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/inventory/pm-pbo/${id}`);
  }

  // ========== IA THRESHOLDS ==========

  getThresholds(): Observable<Threshold[]> {
    return this.http.get<Threshold[]>(`${this.baseUrl}/thresholds`);
  }

  updateThreshold(id: number | string, data: any): Observable<Threshold> {
    return this.http.put<Threshold>(`${this.baseUrl}/thresholds/${id}`, data);
  }

  getChatLogs(): Observable<ChatLog[]> {
    return this.http.get<ChatLog[]>(`${this.baseUrl}/chatbot/logs`);
  }

  // ========== INCIDENTS ==========

  getIncidents(): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventory/incidents`);
  }

  getIncidentsPaginated(page: number = 0, size: number = 20, sortBy: string = 'id', direction: string = 'asc'): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get(`${this.baseUrl}/inventory/incidents/paginated`, { params });
  }

  createIncident(incident: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/inventory/incidents`, incident);
  }

  updateIncident(id: number, incident: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/inventory/incidents/${id}`, incident);
  }

  updateIncidentStatus(id: number, status: string): Observable<any> {
    return this.http.patch(`${this.baseUrl}/inventory/incidents/${id}/status`, { status });
  }

  deleteIncident(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/inventory/incidents/${id}`);
  }

  // ========== DIAGNOSTIC IA ==========

  diagnoseOnt(id: number, includeSimilarCases: boolean = true): Observable<any> {
    return this.http.post(`${this.baseUrl}/ai/diagnostics`, {
      resourceId: id,
      resourceType: 'ONT',
      includeSimilarCases: includeSimilarCases
    });
  }

  diagnoseSplitter(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/ai/diagnostics`, {
      resourceId: id,
      resourceType: 'SPLITTER'
    });
  }

  diagnoseOlt(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/ai/diagnostics`, {
      resourceId: id,
      resourceType: 'OLT'
    });
  }

  diagnosePon(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/ai/diagnostics`, {
      resourceId: id,
      resourceType: 'PON'
    });
  }

  getAiRules(): Observable<any> {
    return this.http.get(`${this.baseUrl}/ai/rules`);
  }

  // ========== CHATBOT (FIXED) ==========
  queryChatbot(text: string, sessionId?: string, username?: string | null): Observable<any> {
    const payload = {
      text: text,
      sessionId: sessionId || null,
      username: username || null
    };

    return this.http.post<any>(`${this.baseUrl}/chatbot/query`, payload);
  }

  // ========== MONITORING ==========

  getPonSummary(ponId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/monitoring/pon/${ponId}/summary`);
  }

  getOltSummary(oltId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/monitoring/olt/${oltId}/summary`);
  }

  getSplitterSummary(splitterId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/monitoring/splitter/${splitterId}/summary`);
  }

  getPonTopology(ponId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/monitoring/pon/${ponId}/topology`);
  }
}