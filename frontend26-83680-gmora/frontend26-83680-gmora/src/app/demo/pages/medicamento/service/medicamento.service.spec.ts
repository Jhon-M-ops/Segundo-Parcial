import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { MedicamentoService } from './medicamento.service';

describe('MedicamentoService', () => {
  let service: MedicamentoService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(MedicamentoService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('requests the medication list from the backend API', () => {
    service.listar().subscribe((medicamentos) => expect(medicamentos).toEqual([]));

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/medicamento/listar'
    );
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('creates a medication sending only the business fields', () => {
    service.guardar({
      id: null,
      nombre: 'Ivermectina',
      descripcion: 'Antiparasitario de uso tópico',
      presentacion: 'Frasco 10 mL',
      fechaCompra: '2025-01-10',
      fechaVence: '2026-01-10'
    }).subscribe((respuesta) => expect(respuesta.status).toBe(200));

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/medicamento/guardar'
    );
    expect(request.request.method).toBe('POST');
    request.flush({ status: 200, message: 'Medicamento guardado correctamente' });
  });
});