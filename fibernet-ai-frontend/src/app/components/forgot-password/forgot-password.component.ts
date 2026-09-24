import { Component } from '@angular/core';
import { NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  email = '';
  loading = false;
  error = '';
  success = '';

  private readonly baseUrl = 'http://localhost:8081/api/auth';

  constructor(private http: HttpClient) {}

  onSubmit(form: NgForm): void {
    if (form.invalid || this.loading) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    this.http.post(`${this.baseUrl}/forgot-password`, { email: this.email }).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Un email vous a ete envoye.';
      },
      error: (err) => {
        this.loading = false;
        this.error = this.getErrorMessage(err, 'Email introuvable ou requete invalide.');
      }
    });
  }

  private getErrorMessage(err: any, fallback: string): string {
    return err?.error?.message || err?.message || fallback;
  }
}
