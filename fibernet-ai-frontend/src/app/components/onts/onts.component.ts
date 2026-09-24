import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-onts',
  templateUrl: './onts.component.html',
  styleUrls: ['./onts.component.css']
})
export class OntsComponent implements OnInit {
  onts: any[] = [];
  loading = true;
  error: string | null = null;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  
  // Tri
  sortBy = 'id';
  sortDirection = 'asc';
  
  // Filtres
  filters = {
    status: [] as string[],
    minRxPower: null as number | null,
    maxRxPower: null as number | null,
    minTxPower: null as number | null,
    maxTxPower: null as number | null,
    minDistance: null as number | null,
    maxDistance: null as number | null,
    searchSerial: ''
  };
  
  // Options pour les selects
  statusOptions = ['online', 'offline', 'degraded'];
  
  // Afficher/masquer le panneau de filtres
  showFilters = false;
  criticalMode = false;

  newOnt: any = {
    serial: '',
    rxPower: -24,
    txPower: 1.5,
    distanceKm: 2,
    status: 'online'
  };
  
  Math = Math;

  constructor(private apiService: ApiService, private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const status = (params.get('status') || '').toLowerCase();
      const healthStatus = (params.get('healthStatus') || '').toUpperCase();

      if (healthStatus === 'CRITICAL') {
        this.criticalMode = true;
        this.showFilters = false;
        this.filters.status = [];
        this.filters.searchSerial = '';
        this.currentPage = 0;
        this.loadCriticalOnts();
        return;
      }

      this.criticalMode = false;
      if (status && this.statusOptions.includes(status)) {
        this.filters.status = [status];
        this.showFilters = true;
        this.applyFilters();
        return;
      }

      this.filters.status = [];
      this.filters.searchSerial = '';
      this.currentPage = 0;
      this.loadOnts();
    });
  }

  loadCriticalOnts(): void {
    this.loading = true;
    this.error = null;
    this.apiService.getCriticalOnts().subscribe({
      next: (data) => {
        const list = Array.isArray(data) ? data : (data?.content || []);
        this.onts = list;
        this.totalElements = list.length;
        this.totalPages = this.totalElements > 0 ? 1 : 0;
        this.currentPage = 0;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur ONTs critiques:', err);
        this.error = 'Impossible de charger les ONTs critiques';
        this.loading = false;
      }
    });
  }

  loadOnts(): void {
    this.loading = true;
    this.apiService.getOnts(this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
      next: (data) => {
        this.onts = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.loading = false;
        console.log('ONTs:', data);
      },
      error: (err) => {
        console.error('Erreur:', err);
        this.error = 'Impossible de charger les ONTs';
        this.loading = false;
      }
    });
  }

  private toOptionalNumber(value: number | null): number | null {
    if (value === null || value === undefined || value === ('' as unknown as number)) {
      return null;
    }

    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }

  private matchesFrontendFilters(ont: any, serial: string): boolean {
    const ontSerial = String(ont?.serial ?? '').toLowerCase();
    const ontStatus = String(ont?.status ?? '').toLowerCase();

    if (serial !== '' && !ontSerial.includes(serial.toLowerCase())) {
      return false;
    }

    if (this.filters.status.length > 0 && !this.filters.status.includes(ontStatus)) {
      return false;
    }

    const minRxPower = this.toOptionalNumber(this.filters.minRxPower);
    const maxRxPower = this.toOptionalNumber(this.filters.maxRxPower);
    const minTxPower = this.toOptionalNumber(this.filters.minTxPower);
    const maxTxPower = this.toOptionalNumber(this.filters.maxTxPower);
    const minDistance = this.toOptionalNumber(this.filters.minDistance);
    const maxDistance = this.toOptionalNumber(this.filters.maxDistance);

    if (minRxPower !== null && Number(ont?.rxPower) < minRxPower) {
      return false;
    }
    if (maxRxPower !== null && Number(ont?.rxPower) > maxRxPower) {
      return false;
    }
    if (minTxPower !== null && Number(ont?.txPower) < minTxPower) {
      return false;
    }
    if (maxTxPower !== null && Number(ont?.txPower) > maxTxPower) {
      return false;
    }
    if (minDistance !== null && Number(ont?.distanceKm) < minDistance) {
      return false;
    }
    if (maxDistance !== null && Number(ont?.distanceKm) > maxDistance) {
      return false;
    }

    return true;
  }

  applyFilters(resetPage: boolean = true): void {
    this.loading = true;
    if (resetPage) {
      this.currentPage = 0;
    }

    const serial = (this.filters.searchSerial || '').trim();
    const filterParams: any = {};

    if (this.filters.status.length > 0) {
      filterParams.status = [...this.filters.status];
    }
    const minRxPower = this.toOptionalNumber(this.filters.minRxPower);
    const maxRxPower = this.toOptionalNumber(this.filters.maxRxPower);
    const minTxPower = this.toOptionalNumber(this.filters.minTxPower);
    const maxTxPower = this.toOptionalNumber(this.filters.maxTxPower);
    const minDistance = this.toOptionalNumber(this.filters.minDistance);
    const maxDistance = this.toOptionalNumber(this.filters.maxDistance);

    if (minRxPower !== null) {
      filterParams.minRxPower = minRxPower;
    }
    if (maxRxPower !== null) {
      filterParams.maxRxPower = maxRxPower;
    }
    if (minTxPower !== null) {
      filterParams.minTxPower = minTxPower;
    }
    if (maxTxPower !== null) {
      filterParams.maxTxPower = maxTxPower;
    }
    if (minDistance !== null) {
      filterParams.minDistance = minDistance;
    }
    if (maxDistance !== null) {
      filterParams.maxDistance = maxDistance;
    }
    if (serial !== '') {
      filterParams.searchSerial = serial;
    }

    console.log('Filtres appliques:', filterParams);

    this.apiService.filterOnts(
      filterParams,
      this.currentPage,
      this.pageSize,
      this.sortBy,
      this.sortDirection
    ).subscribe({
      next: (data) => {
        const filteredContent = (data.content || []).filter((ont: any) => this.matchesFrontendFilters(ont, serial));

        this.onts = filteredContent;
        this.totalElements = filteredContent.length;
        this.totalPages = filteredContent.length > 0 ? 1 : 0;
        this.loading = false;
        this.error = null;
      },
      error: (err) => {
        console.error('Erreur filtrage ONT:', err);
        this.error = 'Erreur lors du filtrage des ONTs';
        this.loading = false;
      }
    });
  }

  resetFilters(): void {
    this.filters = {
      status: [],
      minRxPower: null,
      maxRxPower: null,
      minTxPower: null,
      maxTxPower: null,
      minDistance: null,
      maxDistance: null,
      searchSerial: ''
    };
    this.currentPage = 0;
    this.loadOnts();
  }

  toggleStatus(status: string): void {
    const index = this.filters.status.indexOf(status);
    if (index === -1) {
      this.filters.status.push(status);
    } else {
      this.filters.status.splice(index, 1);
    }
  }

  isStatusSelected(status: string): boolean {
    return this.filters.status.includes(status);
  }

  changePage(page: number): void {
    if (this.criticalMode) {
      return;
    }

    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      if (this.hasActiveFilters()) {
        this.applyFilters(false);
      } else {
        this.loadOnts();
      }
    }
  }

  changeSort(field: string): void {
    if (this.criticalMode) {
      return;
    }

    if (this.sortBy === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDirection = 'asc';
    }
    this.currentPage = 0;
    if (this.hasActiveFilters()) {
      this.applyFilters(false);
    } else {
      this.loadOnts();
    }
  }

  hasActiveFilters(): boolean {
    return this.filters.status.length > 0 ||
           this.filters.minRxPower !== null ||
           this.filters.maxRxPower !== null ||
           this.filters.minTxPower !== null ||
           this.filters.maxTxPower !== null ||
           this.filters.minDistance !== null ||
           this.filters.maxDistance !== null ||
           this.filters.searchSerial.trim() !== '';
  }

  deleteOnt(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet ONT ?')) {
      this.apiService.deleteOnt(id).subscribe({
        next: () => {
          if (this.criticalMode) {
            this.loadCriticalOnts();
          } else if (this.hasActiveFilters()) {
            this.applyFilters();
          } else {
            this.loadOnts();
          }
          alert('ONT supprimé avec succès !');
        },
        error: (err) => {
          console.error('Erreur:', err);
          alert('Erreur lors de la suppression');
        }
      });
    }
  }

  addOnt(): void {
    if (!this.newOnt.serial || !this.newOnt.status) {
      alert('Veuillez remplir les champs obligatoires');
      return;
    }

    this.apiService.createOnt(this.newOnt).subscribe({
      next: () => {
        this.newOnt = {
          serial: '',
          rxPower: -24,
          txPower: 1.5,
          distanceKm: 2,
          status: 'online'
        };
        const modalElement = document.getElementById('addOntModal');
        if (modalElement) {
          // @ts-ignore
          const bsModal = bootstrap.Modal.getInstance(modalElement);
          if (bsModal) {
            bsModal.hide();
          }
        }

        if (this.criticalMode) {
          this.loadCriticalOnts();
        } else if (this.hasActiveFilters()) {
          this.applyFilters();
        } else {
          this.loadOnts();
        }
      },
      error: (err) => {
        console.error('Erreur ajout ONT:', err);
        alert('Erreur lors de l\'ajout de l\'ONT');
      }
    });
  }

  getHealthBadgeClass(healthStatus: string): string {
    switch (healthStatus) {
      case 'CRITICAL': return 'bg-danger';
      case 'WARNING': return 'bg-warning text-dark';
      case 'GOOD': return 'bg-success';
      default: return 'bg-secondary';
    }
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'online': return 'bg-success';
      case 'offline': return 'bg-danger';
      case 'degraded': return 'bg-warning text-dark';
      default: return 'bg-secondary';
    }
  }
}