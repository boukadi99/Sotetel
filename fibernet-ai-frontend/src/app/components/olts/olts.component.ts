import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-olts',
  templateUrl: './olts.component.html',
  styleUrls: ['./olts.component.css']
})
export class OltsComponent implements OnInit {
  olts: any[] = [];
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
  
  // Nouvel OLT
  newOlt: any = { site: '', vendor: 'Nokia', totalPorts: 8 };
  editOlt: any = { id: null, site: '', vendor: 'Nokia', totalPorts: 8 };
  
  // Pour les calculs dans le template
  Math = Math;

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadOlts();
  }

  loadOlts(): void {
    this.loading = true;
    this.apiService.getOlts(this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
      next: (data) => {
        this.olts = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.loading = false;
        console.log('OLTs:', data);
      },
      error: (err) => {
        console.error('Erreur:', err);
        this.error = 'Impossible de charger les OLTs';
        this.loading = false;
      }
    });
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadOlts();
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
    this.loadOlts();
  }

  addOlt(): void {
    if (!this.newOlt.site || !this.newOlt.vendor || !this.newOlt.totalPorts) {
      alert('Veuillez remplir tous les champs');
      return;
    }
    
    this.apiService.createOlt(this.newOlt).subscribe({
      next: () => {
        this.loadOlts();
        this.newOlt = { site: '', vendor: 'Nokia', totalPorts: 8 };
        // Fermer le modal
        const modalElement = document.getElementById('addOltModal');
        if (modalElement) {
          // @ts-ignore
          const bsModal = bootstrap.Modal.getInstance(modalElement);
          if (bsModal) {
            bsModal.hide();
          }
        }
        alert('OLT ajouté avec succès !');
      },
      error: (err) => {
        console.error('Erreur:', err);
        alert('Erreur lors de l\'ajout de l\'OLT');
      }
    });
  }

  openEditModal(olt: any): void {
    this.editOlt = {
      id: olt.id,
      site: olt.site,
      vendor: olt.vendor,
      totalPorts: olt.totalPorts
    };

    const modalElement = document.getElementById('editOltModal');
    if (modalElement) {
      // @ts-ignore
      const bsModal = new bootstrap.Modal(modalElement);
      bsModal.show();
    }
  }

  updateOlt(): void {
    if (!this.editOlt.id) {
      return;
    }

    this.apiService.updateOlt(this.editOlt.id, this.editOlt).subscribe({
      next: () => {
        this.loadOlts();
        const modalElement = document.getElementById('editOltModal');
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
        alert('Erreur lors de la modification de l\'OLT');
      }
    });
  }

  deleteOlt(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet OLT ?')) {
      this.apiService.deleteOlt(id).subscribe({
        next: () => {
          this.loadOlts();
          alert('OLT supprimé avec succès !');
        },
        error: (err) => {
          console.error('Erreur lors de la suppression:', err);
          alert('Erreur lors de la suppression de l\'OLT');
        }
      });
    }
  }
}