import { AfterViewInit, Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import * as L from 'leaflet';
import { ApiService } from '../../services/api.service';
import { PmPbo } from '../../models/pm-pbo';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mapContainer', { static: false }) mapContainer?: ElementRef<HTMLDivElement>;

  private map?: L.Map;
  private markersLayer = L.layerGroup();

  pmPboList: PmPbo[] = [];
  loading = true;
  error: string | null = null;

  private readonly locationStorageKey = 'fibernet_ai_map_locations';

  // ✅ Dictionnaire pour les localisations manuelles (sans BDD)
  locationCoordinates: Record<string, [number, number]> = {};
  locationMetadata: Record<string, { type: string; capacity: number }> = {};

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadSavedLocations();
    this.loadPmPbo();
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
      this.map = undefined;
    }
  }

  loadPmPbo(): void {
    this.apiService.getPmPbo().subscribe({
      next: (data: PmPbo[]) => {
        this.pmPboList = data;
        this.loading = false;
        this.refreshMarkers();
      },
      error: (err) => {
        console.error('Erreur:', err);
        this.error = 'Impossible de charger les données PM/PBO';
        this.loading = false;
      }
    });
  }

  private initMap(): void {
    if (this.map) {
      return;
    }

    if (!this.mapContainer?.nativeElement) {
      console.error('Élément map non trouvé');
      return;
    }

    try {
      this.map = L.map(this.mapContainer.nativeElement, {
        center: [34.0, 9.0],
        zoom: 7,
        zoomControl: true
      });

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
        maxZoom: 19
      }).addTo(this.map);

      this.markersLayer.addTo(this.map);
      this.refreshMarkers();

      setTimeout(() => this.map?.invalidateSize(), 100);
      setTimeout(() => this.map?.invalidateSize(), 500);
    } catch (err) {
      console.error('Erreur initialisation carte:', err);
      this.error = 'Erreur lors de l\'initialisation de la carte';
    }
  }

  @HostListener('window:resize')
  onResize(): void {
    this.map?.invalidateSize();
  }

  private getPointCoordinates(point: any): [number, number] | null {
    const location = String(point?.location ?? '').trim();
    const localCoords = this.locationCoordinates[location];
    if (localCoords) {
      return localCoords;
    }

    const latitude = Number(point?.latitude ?? point?.lat);
    const longitude = Number(point?.longitude ?? point?.lng);
    if (Number.isFinite(latitude) && Number.isFinite(longitude)) {
      return [latitude, longitude];
    }

    return null;
  }

  private refreshMarkers(): void {
    if (!this.map) {
      return;
    }

    this.markersLayer.clearLayers();

    const bounds = L.latLngBounds([]);
    let hasValidMarkers = false;

    // ✅ Ajouter les marqueurs depuis la BDD (avec latitude/longitude)
    this.pmPboList.forEach((pm) => {
      const coords = this.getPointCoordinates(pm);
      if (coords) {
        const marker = L.circleMarker(coords, {
          radius: 8,
          color: '#ffffff',
          weight: 2,
          fillColor: this.getMarkerColor(pm.type),
          fillOpacity: 0.95
        });

        marker.bindPopup(this.buildPopupContent(pm));
        marker.addTo(this.markersLayer);
        bounds.extend(coords);
        hasValidMarkers = true;
      }
    });



    if (hasValidMarkers) {
      this.map.fitBounds(bounds.pad(0.2));
    } else {
      this.map.setView([34.0, 9.0], 7);
    }
  }



  private loadSavedLocations(): void {
    try {
      const raw = localStorage.getItem(this.locationStorageKey);
      if (!raw) {
        this.locationCoordinates = {};
        this.locationMetadata = {};
        return;
      }

      const parsed = JSON.parse(raw);
      if (!parsed || typeof parsed !== 'object') {
        this.locationCoordinates = {};
        this.locationMetadata = {};
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
      this.locationMetadata = {};
    } catch {
      this.locationCoordinates = {};
      this.locationMetadata = {};
    }
  }



  private buildPopupContent(point: PmPbo): string {
    const coords = this.getPointCoordinates(point);
    const latText = coords ? coords[0].toFixed(5) : '-';
    const lngText = coords ? coords[1].toFixed(5) : '-';

    return `
      <div class="popup-content">
        <strong>${point.type} #${point.id}</strong><br>
        Localisation: ${point.location}<br>
        Capacite: ${point.capacity}<br>
        📍 ${latText}, ${lngText}
      </div>
    `;
  }

  private getMarkerColor(type: string): string {
    switch (type) {
      case 'PIO': return '#3b82f6';
      case 'PBO': return '#22c55e';
      case 'PBI': return '#f97316';
      case 'PM': return '#ef4444';
      default: return '#6b7280';
    }
  }
}
