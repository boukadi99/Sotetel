import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { OntService } from './ont.service';

describe('OntService', () => {
  let service: OntService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(OntService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
