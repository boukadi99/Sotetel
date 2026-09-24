import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';

import { OntsComponent } from './onts.component';

describe('OntsComponent', () => {
  let component: OntsComponent;
  let fixture: ComponentFixture<OntsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [OntsComponent],
      imports: [RouterTestingModule, FormsModule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(OntsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
