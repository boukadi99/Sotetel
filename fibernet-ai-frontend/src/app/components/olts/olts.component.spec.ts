import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';

import { OltsComponent } from './olts.component';

describe('OltsComponent', () => {
  let component: OltsComponent;
  let fixture: ComponentFixture<OltsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [OltsComponent],
      imports: [RouterTestingModule, FormsModule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(OltsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
