import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface UserAccount {
  id: number;
  username: string;
  email: string;
  role: string;
  createdAt: string;
}

interface CreateUserPayload {
  username: string;
  email: string;
  password: string;
  role: string;
}

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  users: UserAccount[] = [];
  loading = false;
  error = '';

  addModalOpen = false;
  addLoading = false;
  addError = '';
  newUser: CreateUserPayload = {
    username: '',
    email: '',
    password: '',
    role: 'TECH'
  };

  actionLoadingId: number | null = null;
  actionType: 'reset' | 'delete' | null = null;

  currentPage = 1;
  pageSize = 8;

  private readonly baseUrl = 'http://localhost:8081/api/auth/admin/users';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = '';

    this.http.get<UserAccount[]>(this.baseUrl).subscribe({
      next: (data) => {
        this.users = data || [];
        this.loading = false;
        this.normalizePagination();
      },
      error: (err) => {
        this.loading = false;
        this.error = this.getErrorMessage(err, 'Impossible de charger les utilisateurs.');
      }
    });
  }

  openAddModal(): void {
    this.addError = '';
    this.addModalOpen = true;
    this.newUser = {
      username: '',
      email: '',
      password: '',
      role: 'TECH'
    };
  }

  closeAddModal(): void {
    if (this.addLoading) {
      return;
    }
    this.addModalOpen = false;
  }

  createUser(): void {
    if (this.addLoading) {
      return;
    }

    if (!this.newUser.username || !this.newUser.email || !this.newUser.password || !this.newUser.role) {
      this.addError = 'Veuillez remplir tous les champs.';
      return;
    }

    this.addLoading = true;
    this.addError = '';

    this.http.post<UserAccount>(this.baseUrl, this.newUser).subscribe({
      next: () => {
        this.addLoading = false;
        this.addModalOpen = false;
        this.loadUsers();
      },
      error: (err) => {
        this.addLoading = false;
        this.addError = this.getErrorMessage(err, 'Creation impossible. Verifiez les informations.');
      }
    });
  }

  resetPassword(user: UserAccount): void {
    const newPassword = window.prompt(`Nouveau mot de passe pour ${user.username}`);
    if (!newPassword) {
      return;
    }

    this.actionLoadingId = user.id;
    this.actionType = 'reset';
    this.error = '';

    this.http.post(`${this.baseUrl}/${user.id}/reset-password`, { password: newPassword }).subscribe({
      next: () => {
        this.actionLoadingId = null;
        this.actionType = null;
      },
      error: (err) => {
        this.actionLoadingId = null;
        this.actionType = null;
        this.error = this.getErrorMessage(err, 'Echec de reinitialisation du mot de passe.');
      }
    });
  }

  deleteUser(user: UserAccount): void {
    const confirmed = window.confirm(`Supprimer l'utilisateur ${user.username} ?`);
    if (!confirmed) {
      return;
    }

    this.actionLoadingId = user.id;
    this.actionType = 'delete';
    this.error = '';

    this.http.delete(`${this.baseUrl}/${user.id}`).subscribe({
      next: () => {
        this.actionLoadingId = null;
        this.actionType = null;
        this.users = this.users.filter((item) => item.id !== user.id);
        this.normalizePagination();
      },
      error: (err) => {
        this.actionLoadingId = null;
        this.actionType = null;
        this.error = this.getErrorMessage(err, 'Suppression impossible.');
      }
    });
  }

  get paginatedUsers(): UserAccount[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.users.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.users.length / this.pageSize));
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  setPage(page: number): void {
    this.currentPage = page;
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage -= 1;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage += 1;
    }
  }

  getRoleBadgeClass(role: string): string {
    return role?.toUpperCase() === 'ADMIN' ? 'bg-danger' : 'bg-primary';
  }

  isActionLoading(user: UserAccount, type: 'reset' | 'delete'): boolean {
    return this.actionLoadingId === user.id && this.actionType === type;
  }

  private normalizePagination(): void {
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }
  }

  private getErrorMessage(err: any, fallback: string): string {
    return err?.error?.message || err?.message || fallback;
  }
}
