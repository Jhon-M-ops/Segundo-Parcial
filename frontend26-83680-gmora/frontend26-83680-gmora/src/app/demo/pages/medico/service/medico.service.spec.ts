import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { MedicoService } from './medico.service';

describe('MedicoService', () => {
  let service: MedicoService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(MedicoService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('requests the doctor list from the backend API', () => {
    service.listar().subscribe((medicos) => expect(medicos).toEqual([]));

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/medico/listar'
    );
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('updates a doctor through the actualizar endpoint', () => {
    service.actualizar({
      id: 1,
      tipoDocumento: 'CC',
      numeroDocumento: '1000000001',
      nombres: 'Giovanni',
      apellidos: 'Mora',
      telefono: '3001112233',
      registroProfesional: 'MV-1001',
      especializacionId: 1
    }).subscribe((respuesta) => expect(respuesta.status).toBe(200));

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/medico/actualizar'
    );
    expect(request.request.method).toBe('POST');
    request.flush({ status: 200, message: 'Médico actualizado correctamente' });
  });
});