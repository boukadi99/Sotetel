import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
	selector: 'app-pons',
	templateUrl: './pons.component.html',
	styleUrls: ['./pons.component.css']
})
export class PonsComponent implements OnInit {
	pons: any[] = [];
	private allPons: any[] = [];
	private clientMode = false;
	loading = true;
	error: string | null = null;

	currentPage = 0;
	pageSize = 10;
	totalPages = 0;
	totalElements = 0;

	sortBy = 'id';
	sortDirection = 'asc';

	newPon: any = { oltId: null, splitterCount: 0 };
	editPon: any = { id: null, oltId: null, splitterCount: 0 };

	constructor(private apiService: ApiService) { }

	ngOnInit(): void {
		this.loadPons();
	}

	private applyClientView(): void {
		const sorted = [...this.allPons].sort((a, b) => {
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
		this.pons = sorted.slice(start, start + this.pageSize);
		this.loading = false;
		this.error = null;
	}

	private loadPonsFallback(): void {
		this.apiService.getPons().subscribe({
			next: (data) => {
				this.clientMode = true;
				this.allPons = Array.isArray(data) ? data : (data?.content || []);
				this.applyClientView();
			},
			error: (err) => {
				console.error('Erreur fallback PON:', err);
				this.error = 'Impossible de charger les PONs';
				this.loading = false;
			}
		});
	}

	loadPons(forceRefresh: boolean = true): void {
		if (this.clientMode && !forceRefresh) {
			this.applyClientView();
			return;
		}

		this.loading = true;
		this.error = null;

		if (this.clientMode) {
			this.loadPonsFallback();
			return;
		}

		this.apiService.getPonsPaginated(this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
			next: (data) => {
				this.clientMode = false;
				this.pons = data.content || [];
				this.totalPages = data.totalPages || 0;
				this.totalElements = data.totalElements || 0;
				this.loading = false;
			},
			error: (err) => {
				console.warn('Endpoint pagine PON indisponible, fallback liste:', err?.status);
				this.loadPonsFallback();
			}
		});
	}

	changePage(page: number): void {
		if (page >= 0 && page < this.totalPages) {
			this.currentPage = page;
			if (this.clientMode) {
				this.loadPons(false);
			} else {
				this.loadPons();
			}
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
		if (this.clientMode) {
			this.loadPons(false);
		} else {
			this.loadPons();
		}
	}

	addPon(): void {
		this.apiService.createPon(this.newPon).subscribe({
			next: () => {
				this.loadPons(true);
				this.newPon = { oltId: null, splitterCount: 0 };
				const modalElement = document.getElementById('addPonModal');
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
				alert('Erreur lors de l\'ajout du PON');
			}
		});
	}

	openEditModal(pon: any): void {
		this.editPon = {
			id: pon.id,
			oltId: pon.oltId,
			splitterCount: pon.splitterCount
		};

		const modalElement = document.getElementById('editPonModal');
		if (modalElement) {
			// @ts-ignore
			const bsModal = new bootstrap.Modal(modalElement);
			bsModal.show();
		}
	}

	updatePon(): void {
		if (!this.editPon.id) {
			return;
		}

		this.apiService.updatePon(this.editPon.id, this.editPon).subscribe({
			next: () => {
				this.loadPons(true);
				const modalElement = document.getElementById('editPonModal');
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
				alert('Erreur lors de la modification du PON');
			}
		});
	}

	deletePon(id: number): void {
		if (confirm('Etes-vous sur de vouloir supprimer ce PON ?')) {
			this.apiService.deletePon(id).subscribe({
				next: () => this.loadPons(true),
				error: (err) => {
					console.error('Erreur:', err);
					alert('Erreur lors de la suppression du PON');
				}
			});
		}
	}
}
