import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  token = '';
  password = '';
  confirmPassword = '';
  loading = false;
  error = '';
  success = '';

  private readonly baseUrl = 'http://localhost:8081/api/auth';

  constructor(private route: ActivatedRoute, private router: Router, private http: HttpClient) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.error = 'Token manquant ou invalide.';
    }
  }

  onSubmit(form: NgForm): void {
    if (form.invalid || this.loading || !this.token) {
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.error = 'Les mots de passe ne correspondent pas.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    this.http.post(`${this.baseUrl}/reset-password`, { token: this.token, newPassword: this.password }).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Mot de passe reinitialise. Redirection vers la connexion...';
        window.setTimeout(() => {
          this.router.navigate(['/login']);
        }, 3000);
      },
      error: (err) => {
        this.loading = false;
        this.error = this.getErrorMessage(err, 'Token invalide ou expire.');
      }
    });
  }

  private getErrorMessage(err: any, fallback: string): string {
    return err?.error?.message || err?.message || fallback;
  }
}
