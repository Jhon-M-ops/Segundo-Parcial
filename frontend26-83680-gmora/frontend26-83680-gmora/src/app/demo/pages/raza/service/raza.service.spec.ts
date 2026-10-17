import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { RazaService } from './raza.service';

describe('RazaService', () => {
  let service: RazaService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(RazaService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('requests the breed list from the backend API', () => {
    service.listar().subscribe((razas) => expect(razas).toEqual([]));

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/raza/listar'
    );
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('creates a breed through the guardar endpoint', () => {
    service.guardar({ razaId: null, nombre: 'Golden Retriever', especie: 'Canino' }).subscribe(
      (respuesta) => expect(respuesta.message).toContain('creada')
    );

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/raza/guardar'
    );
    expect(request.request.method).toBe('POST');
    request.flush({ status: 200, message: 'Raza creada exitosamente' });
  });
});