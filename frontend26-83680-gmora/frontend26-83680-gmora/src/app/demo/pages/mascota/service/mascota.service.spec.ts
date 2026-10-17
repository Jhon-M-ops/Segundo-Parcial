import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { MascotaService } from './mascota.service';

describe('MascotaService', () => {
  let service: MascotaService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(MascotaService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('requests the pet list from the backend API', () => {
    service.listar().subscribe((mascotas) => expect(mascotas).toEqual([]));

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/mascota/listar'
    );
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('creates a pet through the guardar endpoint', () => {
    service.guardar({
      mascotaId: null,
      nombreMascota: 'Firulais',
      edad: 4,
      razaId: 1,
      clienteId: 1
    }).subscribe((respuesta) => expect(respuesta.message).toContain('guardada'));

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/mascota/guardar'
    );
    expect(request.request.method).toBe('POST');
    request.flush({ status: 200, message: 'Mascota guardada correctamente' });
  });

  it('deletes a pet sending only the identifier', () => {
    service.eliminar(7).subscribe((respuesta) => expect(respuesta.status).toBe(200));

    const request = httpTestingController.expectOne(
      'http://localhost:8080/clinica/v1/mascota/eliminar'
    );
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ mascotaId: 7 });
    request.flush({ status: 200, message: 'Mascota eliminada correctamente' });
  });
});