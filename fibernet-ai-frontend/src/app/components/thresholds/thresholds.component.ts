import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { Threshold } from '../../models/threshold.model';

interface ThresholdRow {
  id: number | string;
  name: string;
  value: number;
  unit: string;
  description: string;
  technology: string;
  level: string;
  raw: any;
  editing: boolean;
  draftValue: number;
  saving: boolean;
}

interface ThresholdGroup {
  technology: string;
  items: ThresholdRow[];
}

@Component({
  selector: 'app-thresholds',
  templateUrl: './thresholds.component.html',
  styleUrls: ['./thresholds.component.css']
})
export class ThresholdsComponent implements OnInit {
  loading = false;
  error: string | null = null;
  groups: ThresholdGroup[] = [];

  private readonly technologyOrder = ['GPON', 'XGS-PON', 'DEFAULT', 'SPLITTER'];

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadThresholds();
  }

  loadThresholds(): void {
    this.loading = true;
    this.error = null;

    this.apiService.getThresholds().subscribe({
      next: (response) => {
        const rows = this.normalizeRows(response);
        this.groups = this.groupByTechnology(rows);
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement des seuils IA:', err);
        this.error = 'Impossible de charger les seuils IA.';
        this.groups = [];
        this.loading = false;
      }
    });
  }

  startEdit(item: ThresholdRow): void {
    item.editing = true;
    item.draftValue = item.value;
  }

  cancelEdit(item: ThresholdRow): void {
    item.editing = false;
    item.draftValue = item.value;
  }

  save(item: ThresholdRow): void {
    const nextValue = Number(item.draftValue);
    if (!Number.isFinite(nextValue)) {
      this.error = 'La valeur du seuil doit etre numerique.';
      return;
    }

    item.saving = true;
    this.error = null;

    const payload = {
      ...item.raw,
      value: nextValue
    };

    this.apiService.updateThreshold(item.id, payload).subscribe({
      next: (updated) => {
        const normalized = this.normalizeThreshold({ ...item.raw, ...updated, value: nextValue });
        item.value = normalized.value;
        item.draftValue = normalized.value;
        item.unit = normalized.unit;
        item.description = normalized.description;
        item.level = normalized.level;
        item.technology = normalized.technology;
        item.raw = { ...item.raw, ...updated, value: item.value };
        item.editing = false;
        item.saving = false;
      },
      error: (err) => {
        console.error('Erreur sauvegarde seuil IA:', err);
        this.error = `Impossible de sauvegarder le seuil ${item.name}.`;
        item.saving = false;
      }
    });
  }

  getLevelBadgeClass(level: string): string {
    switch ((level || '').toLowerCase()) {
      case 'critical':
        return 'bg-danger';
      case 'degraded':
        return 'bg-warning text-dark';
      case 'warning':
        return 'bg-primary';
      case 'good':
        return 'bg-success';
      default:
        return 'bg-secondary';
    }
  }

  trackByGroup(index: number, group: ThresholdGroup): string {
    return `${index}-${group.technology}`;
  }

  trackByRow(index: number, item: ThresholdRow): string {
    return `${index}-${item.id}-${item.name}`;
  }

  private normalizeRows(response: any): ThresholdRow[] {
    const rawItems = this.extractArray(response);
    return rawItems
      .map((item) => this.normalizeThreshold(item))
      .filter((item) => item.name && Number.isFinite(item.value))
      .map((item) => ({
        ...item,
        editing: false,
        draftValue: item.value,
        saving: false
      }));
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

  private normalizeThreshold(raw: any): ThresholdRow {
    const id = raw?.id ?? raw?.thresholdId ?? raw?.key ?? raw?.name ?? Math.random();
    const name = String(raw?.name ?? raw?.thresholdName ?? raw?.key ?? `Seuil ${id}`).trim();
    const value = Number(raw?.value ?? raw?.thresholdValue ?? raw?.limit ?? 0);
    const unit = String(raw?.unit ?? raw?.measureUnit ?? '').trim();
    const description = String(raw?.description ?? raw?.details ?? '').trim();
    const technology = this.normalizeTechnology(raw?.technology ?? raw?.tech ?? raw?.profile ?? raw?.category);
    const level = this.normalizeLevel(raw, name);

    return {
      id,
      name,
      value,
      unit,
      description,
      technology,
      level,
      raw,
      editing: false,
      draftValue: value,
      saving: false
    };
  }

  private normalizeTechnology(value: unknown): string {
    const raw = String(value ?? 'DEFAULT').trim().toUpperCase();
    if (raw === 'XGS' || raw === 'XGS_PON' || raw === 'XGSPON') {
      return 'XGS-PON';
    }
    if (this.technologyOrder.includes(raw)) {
      return raw;
    }
    return raw || 'DEFAULT';
  }

  private normalizeLevel(raw: any, name: string): string {
    const explicit = String(raw?.level ?? raw?.severity ?? raw?.status ?? '').trim().toLowerCase();
    const validLevels = ['critical', 'degraded', 'warning', 'good'];
    if (validLevels.includes(explicit)) {
      return explicit;
    }

    const source = `${String(raw?.key ?? '')} ${name}`.toLowerCase();
    if (source.includes('critical')) {
      return 'critical';
    }
    if (source.includes('degraded')) {
      return 'degraded';
    }
    if (source.includes('warning')) {
      return 'warning';
    }
    if (source.includes('good')) {
      return 'good';
    }
    return 'warning';
  }

  private groupByTechnology(rows: ThresholdRow[]): ThresholdGroup[] {
    const grouped = new Map<string, ThresholdRow[]>();
    rows.forEach((row) => {
      if (!grouped.has(row.technology)) {
        grouped.set(row.technology, []);
      }
      grouped.get(row.technology)?.push(row);
    });

    return Array.from(grouped.entries())
      .sort((a, b) => this.getTechnologyRank(a[0]) - this.getTechnologyRank(b[0]))
      .map(([technology, items]) => ({
        technology,
        items: items.sort((x, y) => x.name.localeCompare(y.name))
      }));
  }

  private getTechnologyRank(technology: string): number {
    const idx = this.technologyOrder.indexOf(technology);
    return idx === -1 ? this.technologyOrder.length + 1 : idx;
  }

}
