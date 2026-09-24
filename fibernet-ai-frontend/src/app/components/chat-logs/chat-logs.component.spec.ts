import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';

import { ChatLogsComponent } from './chat-logs.component';

describe('ChatLogsComponent', () => {
  let component: ChatLogsComponent;
  let fixture: ComponentFixture<ChatLogsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ChatLogsComponent],
      imports: [RouterTestingModule, FormsModule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(ChatLogsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
