import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// Composants
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
import { RegisterComponent } from './components/register/register.component';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { UserManagementComponent } from './components/user-management/user-management.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    OltsComponent,
    OntsComponent,
    PonsComponent,
    SplittersComponent,
    PmPboComponent,
    IncidentsComponent,
    DiagnosticComponent,
    MapComponent,
    ChatbotComponent,
    ThresholdsComponent,
    ChatLogsComponent,
    LoginComponent,
    RegisterComponent,
    UserManagementComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }