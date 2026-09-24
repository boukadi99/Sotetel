import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';

import { ThresholdsComponent } from './thresholds.component';

describe('ThresholdsComponent', () => {
  let component: ThresholdsComponent;
  let fixture: ComponentFixture<ThresholdsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ThresholdsComponent],
      imports: [RouterTestingModule, FormsModule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(ThresholdsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
