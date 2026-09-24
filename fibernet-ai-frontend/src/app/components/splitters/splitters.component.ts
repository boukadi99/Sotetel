import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
	selector: 'app-splitters',
	templateUrl: './splitters.component.html',
	styleUrls: ['./splitters.component.css']
})
export class SplittersComponent implements OnInit {
	splitters: any[] = [];
	private allSplitters: any[] = [];
	private clientMode = false;
	loading = true;
	error: string | null = null;

	currentPage = 0;
	pageSize = 10;
	totalPages = 0;
	totalElements = 0;

	sortBy = 'id';
	sortDirection = 'asc';

	newSplitter: any = { ratio: '1:8', lossDb: 0, ponId: null, ontCount: 0 };
	editSplitter: any = { id: null, ratio: '1:8', lossDb: 0, ponId: null, ontCount: 0 };

	constructor(private apiService: ApiService) { }

	ngOnInit(): void {
		this.loadSplitters();
	}

	private applyClientView(): void {
		const sorted = [...this.allSplitters].sort((a, b) => {
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
		this.splitters = sorted.slice(start, start + this.pageSize);
		this.loading = false;
		this.error = null;
	}

	private loadSplittersFallback(): void {
		this.apiService.getSplitters().subscribe({
			next: (data) => {
				this.clientMode = true;
				this.allSplitters = Array.isArray(data) ? data : (data?.content || []);
				this.applyClientView();
			},
			error: (err) => {
				console.error('Erreur fallback splitter:', err);
				this.error = 'Impossible de charger les splitters';
				this.loading = false;
			}
		});
	}

	loadSplitters(forceRefresh: boolean = true): void {
		if (this.clientMode && !forceRefresh) {
			this.applyClientView();
			return;
		}

		this.loading = true;
		this.error = null;

		if (this.clientMode) {
			this.loadSplittersFallback();
			return;
		}

		this.apiService.getSplittersPaginated(this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
			next: (data) => {
				this.clientMode = false;
				this.splitters = data.content || [];
				this.totalPages = data.totalPages || 0;
				this.totalElements = data.totalElements || 0;
				this.loading = false;
			},
			error: (err) => {
				console.warn('Endpoint pagine splitter indisponible, fallback liste:', err?.status);
				this.loadSplittersFallback();
			}
		});
	}

	changePage(page: number): void {
		if (page >= 0 && page < this.totalPages) {
			this.currentPage = page;
			if (this.clientMode) {
				this.loadSplitters(false);
			} else {
				this.loadSplitters();
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
			this.loadSplitters(false);
		} else {
			this.loadSplitters();
		}
	}

	addSplitter(): void {
		this.apiService.createSplitter(this.newSplitter).subscribe({
			next: () => {
				this.loadSplitters(true);
				this.newSplitter = { ratio: '1:8', lossDb: 0, ponId: null, ontCount: 0 };
				const modalElement = document.getElementById('addSplitterModal');
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
				alert('Erreur lors de l\'ajout du splitter');
			}
		});
	}

	openEditModal(splitter: any): void {
		this.editSplitter = {
			id: splitter.id,
			ratio: splitter.ratio,
			lossDb: splitter.lossDb,
			ponId: splitter.ponId,
			ontCount: splitter.ontCount
		};

		const modalElement = document.getElementById('editSplitterModal');
		if (modalElement) {
			// @ts-ignore
			const bsModal = new bootstrap.Modal(modalElement);
			bsModal.show();
		}
	}

	updateSplitter(): void {
		if (!this.editSplitter.id) {
			return;
		}

		this.apiService.updateSplitter(this.editSplitter.id, this.editSplitter).subscribe({
			next: () => {
				this.loadSplitters(true);
				const modalElement = document.getElementById('editSplitterModal');
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
				alert('Erreur lors de la modification du splitter');
			}
		});
	}

	deleteSplitter(id: number): void {
		if (confirm('Etes-vous sur de vouloir supprimer ce splitter ?')) {
			this.apiService.deleteSplitter(id).subscribe({
				next: () => this.loadSplitters(true),
				error: (err) => {
					console.error('Erreur:', err);
					alert('Erreur lors de la suppression du splitter');
				}
			});
		}
	}

	getLossBadgeClass(lossDb: number): string {
		if (lossDb > 5) {
			return 'bg-danger';
		}
		if (lossDb > 3) {
			return 'bg-warning text-dark';
		}
		return 'bg-success';
	}
}
