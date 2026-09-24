import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-pm-pbo',
  templateUrl: './pm-pbo.component.html',
  styleUrls: ['./pm-pbo.component.css']
})
export class PmPboComponent implements OnInit {
  points: any[] = [];
  private allPoints: any[] = [];
  loading = true;
  error: string | null = null;

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  sortBy = 'id';
  sortDirection = 'asc';

  types = ['PIO', 'PBO', 'PBI', 'PM'];
  locations = [] as string[];
  private readonly locationStorageKey = 'fibernet_ai_map_locations';
  private locationCoordinates: Record<string, [number, number]> = {};

  // ✅ Ajout des champs latitude et longitude
  newPoint: any = { 
    type: 'PIO', 
    capacity: 12, 
    location: 'Loc_1',
    latitude: null,
    longitude: null
  };
  
  editPoint: any = { 
    id: null, 
    type: 'PIO', 
    capacity: 12, 
    location: 'Loc_1',
    latitude: null,
    longitude: null
  };

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadSavedLocations();
    this.loadPoints();
  }

  private loadSavedLocations(): void {
    try {
      const raw = localStorage.getItem(this.locationStorageKey);
      if (!raw) {
        this.locationCoordinates = {};
        this.locations = [];
        return;
      }

      const parsed = JSON.parse(raw);
      if (!parsed || typeof parsed !== 'object') {
        this.locationCoordinates = {};
        this.locations = [];
        return;
      }

      const sanitized: Record<string, [number, number]> = {};
      Object.entries(parsed).forEach(([key, value]) => {
        if (Array.isArray(value) && value.length === 2) {
          const lat = Number(value[0]);
          const lng = Number(value[1]);
          if (Number.isFinite(lat) && Number.isFinite(lng)) {
            sanitized[key] = [lat, lng];
          }
          return;
        }

        if (value && typeof value === 'object') {
          const locationValue = value as { lat?: unknown; lng?: unknown };
          const lat = Number(locationValue.lat);
          const lng = Number(locationValue.lng);
          if (Number.isFinite(lat) && Number.isFinite(lng)) {
            sanitized[key] = [lat, lng];
          }
        }
      });

      this.locationCoordinates = sanitized;
      this.locations = Object.keys(this.locationCoordinates).sort((a, b) => a.localeCompare(b));
    } catch {
      this.locationCoordinates = {};
      this.locations = [];
    }
  }

  private persistLocations(): void {
    try {
      const raw = localStorage.getItem(this.locationStorageKey);
      const parsed = raw ? JSON.parse(raw) : {};
      const payload: Record<string, any> = parsed && typeof parsed === 'object' ? parsed : {};

      Object.entries(this.locationCoordinates).forEach(([name, coords]) => {
        const existing = payload[name];
        if (existing && typeof existing === 'object' && !Array.isArray(existing)) {
          payload[name] = {
            ...existing,
            lat: coords[0],
            lng: coords[1]
          };
        } else {
          payload[name] = coords;
        }
      });

      localStorage.setItem(this.locationStorageKey, JSON.stringify(payload));
    } catch {
      // ignore persistence errors to avoid blocking PM/PBO flow
    }
  }

  private normalizeAndValidateCoords(latValue: any, lngValue: any): { lat: number; lng: number } | null {
    if (latValue === null || latValue === '' || lngValue === null || lngValue === '') {
      return null;
    }

    const lat = Number(latValue);
    const lng = Number(lngValue);

    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      return null;
    }

    if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
      return null;
    }

    return { lat, lng };
  }

  private ensureLocationCoordinates(location: string, latValue: any, lngValue: any): { lat: number; lng: number } | null {
    const existing = this.locationCoordinates[location];
    const provided = this.normalizeAndValidateCoords(latValue, lngValue);

    if (provided) {
      this.locationCoordinates[location] = [provided.lat, provided.lng];
      this.locations = Object.keys(this.locationCoordinates).sort((a, b) => a.localeCompare(b));
      this.persistLocations();
      return provided;
    }

    if (existing) {
      return { lat: existing[0], lng: existing[1] };
    }

    return null;
  }

  onNewLocationChange(): void {
    const location = (this.newPoint.location || '').trim();
    const coords = this.locationCoordinates[location];
    if (!coords) {
      return;
    }

    this.newPoint.latitude = coords[0];
    this.newPoint.longitude = coords[1];
  }

  onEditLocationChange(): void {
    const location = (this.editPoint.location || '').trim();
    const coords = this.locationCoordinates[location];
    if (!coords) {
      return;
    }

    this.editPoint.latitude = coords[0];
    this.editPoint.longitude = coords[1];
  }

  private resolvePointCoordinates(point: any): [number, number] | null {
    const location = String(point?.location ?? '').trim();
    const fromLocation = this.locationCoordinates[location];
    if (fromLocation) {
      return fromLocation;
    }

    const latitude = Number(point?.latitude ?? point?.lat);
    const longitude = Number(point?.longitude ?? point?.lng);
    if (Number.isFinite(latitude) && Number.isFinite(longitude)) {
      return [latitude, longitude];
    }

    return null;
  }

  getDisplayLatitude(point: any): string {
    const coords = this.resolvePointCoordinates(point);
    return coords ? coords[0].toFixed(5) : '-';
  }

  getDisplayLongitude(point: any): string {
    const coords = this.resolvePointCoordinates(point);
    return coords ? coords[1].toFixed(5) : '-';
  }

  private applyClientView(): void {
    const sorted = [...this.allPoints].sort((a, b) => {
      const aVal = a?.[this.sortBy];
      const bVal = b?.[this.sortBy];

      const aNum = Number(aVal);
      const bNum = Number(bVal);
      let result = 0;

      if (Number.isFinite(aNum) && Number.isFinite(bNum)) {
        result = aNum - bNum;
      } else {
        result = String(aVal ?? '').localeCompare(String(bVal ?? ''));
      }

      return this.sortDirection === 'asc' ? result : -result;
    });

    this.totalElements = sorted.length;
    this.totalPages = Math.ceil(this.totalElements / this.pageSize);

    if (this.totalPages > 0 && this.currentPage >= this.totalPages) {
      this.currentPage = this.totalPages - 1;
    }

    const start = this.currentPage * this.pageSize;
    this.points = sorted.slice(start, start + this.pageSize);
    this.loading = false;
    this.error = null;
  }

  // ✅ MODIFICATION: Utiliser directement l'endpoint simple (sans pagination backend)
  loadPoints(): void {
    this.loading = true;
    this.error = null;

    this.apiService.getPmPbo().subscribe({
      next: (data) => {
        this.allPoints = Array.isArray(data) ? data : (data?.content || []);
        this.applyClientView();  // Pagination côté client
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement PM/PBO:', err);
        this.error = 'Impossible de charger les PM/PBO';
        this.loading = false;
      }
    });
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.applyClientView();
    }
  }

  changeSort(field: string): void {
    if (this.sortBy === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDirection = 'asc';
    }
    this.currentPage = 0;
    this.applyClientView();
  }

  addPoint(): void {
    const location = (this.newPoint.location || '').trim();
    if (!location) {
      alert('La localisation est obligatoire.');
      return;
    }

    const coords = this.ensureLocationCoordinates(location, this.newPoint.latitude, this.newPoint.longitude);
    if (!coords) {
      alert('Veuillez renseigner des coordonnees valides pour cette localisation.');
      return;
    }

    const payload = {
      ...this.newPoint,
      location,
      latitude: coords.lat,
      longitude: coords.lng
    };

    this.apiService.createPmPbo(payload).subscribe({
      next: () => {
        this.loadPoints();
        this.newPoint = { 
          type: 'PIO', 
          capacity: 12, 
          location: 'Loc_1',
          latitude: null,
          longitude: null
        };
        const modalElement = document.getElementById('addPmPboModal');
        if (modalElement) {
          // @ts-ignore
          const bsModal = bootstrap.Modal.getInstance(modalElement);
          if (bsModal) {
            bsModal.hide();
          }
        }
      },
      error: (err) => {
        console.error('Erreur:', err);
        alert('Erreur lors de l\'ajout PM/PBO');
      }
    });
  }

  openEditModal(point: any): void {
    const resolvedCoords = this.resolvePointCoordinates(point);

    this.editPoint = {
      id: point.id,
      type: point.type,
      capacity: point.capacity,
      location: point.location,
      latitude: resolvedCoords ? resolvedCoords[0] : (point.latitude ?? point.lat ?? null),
      longitude: resolvedCoords ? resolvedCoords[1] : (point.longitude ?? point.lng ?? null)
    };

    const modalElement = document.getElementById('editPmPboModal');
    if (modalElement) {
      // @ts-ignore
      const bsModal = new bootstrap.Modal(modalElement);
      bsModal.show();
    }
  }

  updatePoint(): void {
    if (!this.editPoint.id) {
      return;
    }

    const location = (this.editPoint.location || '').trim();
    if (!location) {
      alert('La localisation est obligatoire.');
      return;
    }

    const coords = this.ensureLocationCoordinates(location, this.editPoint.latitude, this.editPoint.longitude);
    if (!coords) {
      alert('Veuillez renseigner des coordonnees valides pour cette localisation.');
      return;
    }

    const payload = {
      ...this.editPoint,
      location,
      latitude: coords.lat,
      longitude: coords.lng
    };

    this.apiService.updatePmPbo(this.editPoint.id, payload).subscribe({
      next: () => {
        this.loadPoints();
        const modalElement = document.getElementById('editPmPboModal');
        if (modalElement) {
          // @ts-ignore
          const bsModal = bootstrap.Modal.getInstance(modalElement);
          if (bsModal) {
            bsModal.hide();
          }
        }
      },
      error: (err) => {
        console.error('Erreur:', err);
        alert('Erreur lors de la modification PM/PBO');
      }
    });
  }

  deletePoint(id: number): void {
    if (confirm('Etes-vous sur de vouloir supprimer ce point ?')) {
      this.apiService.deletePmPbo(id).subscribe({
        next: () => this.loadPoints(),
        error: (err) => {
          console.error('Erreur:', err);
          alert('Erreur lors de la suppression PM/PBO');
        }
      });
    }
  }

  getTypeBadgeClass(type: string): string {
    switch (type) {
      case 'PIO':
        return 'bg-primary';
      case 'PBO':
        return 'bg-success';
      case 'PBI':
        return 'bg-warning text-dark';
      case 'PM':
        return 'bg-danger';
      default:
        return 'bg-secondary';
    }
  }
}