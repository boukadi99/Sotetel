import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  stats: any = {};
  loading = true;
  error: string | null = null;

  constructor(private apiService: ApiService, private router: Router) { }

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.apiService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
        console.log('Dashboard data:', data);
      },
      error: (err) => {
        console.error('Erreur:', err);
        this.error = 'Impossible de charger les données du dashboard';
        this.loading = false;
      }
    });
  }

  goToOlts(): void {
    this.router.navigate(['/olts']);
  }

  goToPons(): void {
    this.router.navigate(['/pons']);
  }

  goToSplitters(): void {
    this.router.navigate(['/splitters']);
  }

  goToOnts(): void {
    this.router.navigate(['/onts']);
  }

  goToOntStatus(status: 'online' | 'offline' | 'degraded'): void {
    this.router.navigate(['/onts'], { queryParams: { status } });
  }

  goToCriticalOnts(): void {
    this.router.navigate(['/onts'], { queryParams: { healthStatus: 'CRITICAL' } });
  }

  goToIncidents(): void {
    this.router.navigate(['/incidents']);
  }
}