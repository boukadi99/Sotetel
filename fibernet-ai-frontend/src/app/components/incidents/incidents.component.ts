import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
	selector: 'app-incidents',
	templateUrl: './incidents.component.html',
	styleUrls: ['./incidents.component.css']
})
export class IncidentsComponent implements OnInit {
	incidents: any[] = [];
	private allIncidents: any[] = [];
	private clientMode = false;
	private localMode = false;
	private readonly localKey = 'fibernet_ai_local_incidents';

	loading = true;
	error: string | null = null;

	currentPage = 0;
	pageSize = 10;
	totalPages = 0;
	totalElements = 0;

	sortBy = 'id';
	sortDirection = 'desc';

	statusOptions = ['PENDING', 'IN_PROGRESS', 'RESOLVED'];
	resourceOptions = ['ONT', 'OLT', 'SPLITTER', 'PM', 'PBO'];
	typeOptions = ['DISCONNECT', 'LOSS_HIGH', 'BREAK', 'RX_LOW'];

	newIncident: any = {
		resource: 'ONT',
		value: null,
		type: 'DISCONNECT',
		recommendation: '',
		status: 'PENDING'
	};

	editIncident: any = {
		id: null,
		resource: 'ONT',
		value: null,
		type: 'DISCONNECT',
		recommendation: '',
		status: 'PENDING'
	};

	constructor(private apiService: ApiService) { }

	ngOnInit(): void {
		this.loadIncidents();
	}

	private normalizeString(...values: any[]): string {
		for (const value of values) {
			if (typeof value === 'string') {
				const trimmed = value.trim();
				if (trimmed !== '' && trimmed !== '-') {
					return trimmed;
				}
			}
		}
		return '';
	}

	private normalizeStatus(rawStatus: any): string {
		const status = String(rawStatus ?? '').toUpperCase();
		if (['PENDING', 'IN_PROGRESS', 'RESOLVED'].includes(status)) {
			return status;
		}
		if (status === 'INPROGRESS') {
			return 'IN_PROGRESS';
		}
		if (status === 'OPEN') {
			return 'PENDING';
		}
		if (status === 'CLOSED' || status === 'DONE') {
			return 'RESOLVED';
		}
		return 'PENDING';
	}

	private mapIncident(raw: any): any {
		const id = Number(
			raw?.id ??
			raw?.incidentId ??
			raw?.alertId ??
			raw?.eventId ??
			0
		);

		const recommendation = this.normalizeString(
			raw?.recommendation,
			raw?.description,
			raw?.details,
			raw?.message,
			raw?.content,
			raw?.comment
		);

		const resource = this.normalizeString(raw?.resource, raw?.resourceType, raw?.entityType).toUpperCase() || 'ONT';
		const type = this.normalizeString(raw?.type, raw?.incidentType, raw?.name, raw?.title).toUpperCase() || 'DISCONNECT';
		const value = Number(raw?.value ?? raw?.metricValue ?? raw?.measuredValue ?? raw?.rxPower ?? 0);

		return {
			...raw,
			id,
			resource,
			value: Number.isFinite(value) ? value : 0,
			type,
			recommendation: recommendation || '-',
			status: this.normalizeStatus(raw?.status ?? raw?.incidentStatus ?? raw?.state),
			createdAt: raw?.createdAt ?? raw?.createdDate ?? raw?.timestamp ?? null
		};
	}

	private normalizeData(data: any): any[] {
		let list: any[] = [];

		if (Array.isArray(data)) {
			list = data;
		} else if (Array.isArray(data?.content)) {
			list = data.content;
		} else if (Array.isArray(data?.data)) {
			list = data.data;
		} else if (Array.isArray(data?.items)) {
			list = data.items;
		}

		return list.map((item) => this.mapIncident(item));
	}

	private loadLocalIncidents(): any[] {
		try {
			const raw = localStorage.getItem(this.localKey);
			if (!raw) {
				return [];
			}
			const parsed = JSON.parse(raw);
			return Array.isArray(parsed) ? parsed : [];
		} catch {
			return [];
		}
	}

	private saveLocalIncidents(data: any[]): void {
		localStorage.setItem(this.localKey, JSON.stringify(data));
	}

	private applyClientView(): void {
		const sorted = [...this.allIncidents].sort((a, b) => {
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
		this.incidents = sorted.slice(start, start + this.pageSize);
		this.loading = false;
		this.error = null;
	}

	private loadIncidentsFallback(): void {
		this.apiService.getIncidents().subscribe({
			next: (data) => {
				this.clientMode = true;
				this.allIncidents = this.normalizeData(data);
				this.applyClientView();
			},
			error: () => {
				this.localMode = true;
				this.clientMode = true;
				this.allIncidents = this.loadLocalIncidents();
				this.applyClientView();
			}
		});
	}

	loadIncidents(forceRefresh: boolean = true): void {
		if (this.clientMode && !forceRefresh) {
			this.applyClientView();
			return;
		}

		this.loading = true;
		this.error = null;

		if (this.localMode) {
			this.allIncidents = this.loadLocalIncidents();
			this.applyClientView();
			return;
		}

		if (this.clientMode) {
			this.loadIncidentsFallback();
			return;
		}

		this.apiService.getIncidentsPaginated(this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
			next: (data) => {
				const content = this.normalizeData(data);
				if (Array.isArray(data?.content)) {
					this.clientMode = false;
					this.incidents = content;
					this.totalPages = data.totalPages || 0;
					this.totalElements = data.totalElements || 0;
					this.loading = false;
					return;
				}

				this.clientMode = true;
				this.allIncidents = content;
				this.applyClientView();
			},
			error: (err) => {
				console.warn('Endpoint pagine incidents indisponible, fallback liste:', err?.status);
				this.loadIncidentsFallback();
			}
		});
	}

	changePage(page: number): void {
		if (page >= 0 && page < this.totalPages) {
			this.currentPage = page;
			if (this.clientMode || this.localMode) {
				this.loadIncidents(false);
			} else {
				this.loadIncidents();
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
		if (this.clientMode || this.localMode) {
			this.loadIncidents(false);
		} else {
			this.loadIncidents();
		}
	}

	addIncident(): void {
		if (!this.newIncident.resource || !this.newIncident.type) {
			alert('Resource et type sont obligatoires');
			return;
		}

		if (this.localMode) {
			const localData = this.loadLocalIncidents();
			const nextId = localData.length > 0 ? Math.max(...localData.map((i) => Number(i.id) || 0)) + 1 : 1;

			localData.push({
				id: nextId,
				...this.newIncident,
				createdAt: new Date().toISOString()
			});

			this.saveLocalIncidents(localData);
			this.allIncidents = localData;
			this.applyClientView();
			this.resetNewIncident();
			this.closeModal('addIncidentModal');
			return;
		}

		this.apiService.createIncident(this.newIncident).subscribe({
			next: () => {
				this.loadIncidents(true);
				this.resetNewIncident();
				this.closeModal('addIncidentModal');
			},
			error: (err) => {
				console.error('Erreur ajout incident:', err);
				alert('Erreur lors de l\'ajout de l\'incident');
			}
		});
	}

	openEditModal(incident: any): void {
		this.editIncident = {
			id: incident.id,
			resource: incident.resource,
			value: incident.value,
			type: incident.type,
			recommendation: incident.recommendation,
			status: incident.status,
		};

		const modalElement = document.getElementById('editIncidentModal');
		if (modalElement) {
			// @ts-ignore
			const bsModal = new bootstrap.Modal(modalElement);
			bsModal.show();
		}
	}

	updateIncident(): void {
		if (!this.editIncident.id) {
			return;
		}

		if (this.localMode) {
			const localData = this.loadLocalIncidents();
			const index = localData.findIndex((i) => i.id === this.editIncident.id);
			if (index !== -1) {
				localData[index] = { ...localData[index], ...this.editIncident };
				this.saveLocalIncidents(localData);
				this.allIncidents = localData;
				this.applyClientView();
			}
			this.closeModal('editIncidentModal');
			return;
		}

		this.apiService.updateIncident(this.editIncident.id, this.editIncident).subscribe({
			next: () => {
				this.loadIncidents(true);
				this.closeModal('editIncidentModal');
			},
			error: (err) => {
				console.error('Erreur maj incident:', err);
				alert('Erreur lors de la modification de l\'incident');
			}
		});
	}

	updateStatus(incident: any, status: string): void {
		if (this.localMode) {
			const localData = this.loadLocalIncidents();
			const index = localData.findIndex((i) => i.id === incident.id);
			if (index !== -1) {
				localData[index].status = status;
				this.saveLocalIncidents(localData);
				this.allIncidents = localData;
				this.applyClientView();
			}
			return;
		}

		this.apiService.updateIncidentStatus(incident.id, status).subscribe({
			next: () => this.loadIncidents(true),
			error: (err) => {
				console.error('Erreur statut incident:', err);
				alert('Erreur lors de la mise a jour du statut');
			}
		});
	}

	deleteIncident(id: number): void {
		if (!confirm('Etes-vous sur de vouloir supprimer cet incident ?')) {
			return;
		}

		if (this.localMode) {
			const localData = this.loadLocalIncidents().filter((i) => i.id !== id);
			this.saveLocalIncidents(localData);
			this.allIncidents = localData;
			this.applyClientView();
			return;
		}

		this.apiService.deleteIncident(id).subscribe({
			next: () => this.loadIncidents(true),
			error: (err) => {
				console.error('Erreur suppression incident:', err);
				alert('Erreur lors de la suppression de l\'incident');
			}
		});
	}

	getStatusBadgeClass(status: string): string {
		switch (String(status || '').toUpperCase()) {
			case 'PENDING': return 'bg-danger';
			case 'IN_PROGRESS': return 'bg-warning text-dark';
			case 'RESOLVED': return 'bg-success';
			default: return 'bg-secondary';
		}
	}

	getTypeBadgeClass(type: string): string {
		switch (String(type || '').toUpperCase()) {
			case 'DISCONNECT': return 'bg-danger';
			case 'LOSS_HIGH': return 'bg-warning text-dark';
			case 'BREAK': return 'bg-dark';
			case 'RX_LOW': return 'bg-info';
			default: return 'bg-secondary';
		}
	}

	private closeModal(id: string): void {
		const modalElement = document.getElementById(id);
		if (!modalElement) {
			return;
		}

		// @ts-ignore
		const bsModal = bootstrap.Modal.getInstance(modalElement);
		if (bsModal) {
			bsModal.hide();
		}
	}

	private resetNewIncident(): void {
		this.newIncident = {
			resource: 'ONT',
			value: null,
			type: 'DISCONNECT',
			recommendation: '',
			status: 'PENDING'
		};
	}
}
