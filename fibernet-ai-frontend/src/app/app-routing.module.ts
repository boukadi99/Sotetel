import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { OltsComponent } from './components/olts/olts.component';
import { OntsComponent } from './components/onts/onts.component';
import { DiagnosticComponent } from './components/diagnostic/diagnostic.component';
import { MapComponent } from './components/map/map.component';
import { PonsComponent } from './components/pons/pons.component';
import { SplittersComponent } from './components/splitters/splitters.component';
import { PmPboComponent } from './components/pm-pbo/pm-pbo.component';
import { IncidentsComponent } from './components/incidents/incidents.component';
import { ChatbotComponent } from './components/chatbot/chatbot.component';
import { ThresholdsComponent } from './components/thresholds/thresholds.component';
import { ChatLogsComponent } from './components/chat-logs/chat-logs.component';
import { LoginComponent } from './components/login/login.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { UserManagementComponent } from './components/user-management/user-management.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { GuestGuard } from './guards/guest.guard';

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canActivate: [GuestGuard] },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'olts', component: OltsComponent, canActivate: [AuthGuard] },
  { path: 'onts', component: OntsComponent, canActivate: [AuthGuard] },
  { path: 'pons', component: PonsComponent, canActivate: [AuthGuard] },
  { path: 'splitters', component: SplittersComponent, canActivate: [AuthGuard] },
  { path: 'pm-pbo', component: PmPboComponent, canActivate: [AuthGuard] },
  { path: 'incidents', component: IncidentsComponent, canActivate: [AuthGuard] },
  { path: 'diagnostic', component: DiagnosticComponent, canActivate: [AuthGuard] },
  { path: 'thresholds', component: ThresholdsComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'chat-logs', component: ChatLogsComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'users', component: UserManagementComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'map', component: MapComponent, canActivate: [AuthGuard] },
  { path: 'assistant-ia', component: ChatbotComponent, canActivate: [AuthGuard] },
  { path: 'chatbot', component: ChatbotComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }