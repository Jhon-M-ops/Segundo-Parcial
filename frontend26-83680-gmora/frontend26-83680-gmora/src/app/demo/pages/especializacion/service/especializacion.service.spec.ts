import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { EspecializacionService } from './especializacion.service';

describe('EspecializacionService', () => {
  let service: EspecializacionService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(EspecializacionService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('requests the specialization list from the backend API', () => {
    service.listar().subscribe((especializaciones) => expect(especializaciones).toEqual([]));

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/especializacion/listar'
    );
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('deletes a specialization sending only the identifier', () => {
    service.eliminar(3).subscribe((respuesta) => expect(respuesta.status).toBe(200));

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/especializacion/eliminar'
    );
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ id: 3 });
    request.flush({ status: 200, message: 'Especialización eliminada correctamente' });
  });
});