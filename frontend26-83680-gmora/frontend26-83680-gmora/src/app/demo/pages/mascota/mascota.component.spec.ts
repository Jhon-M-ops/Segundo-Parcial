import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ClienteService } from '../usuario/service/cliente.service';
import { RazaService } from '../raza/service/raza.service';
import { MascotaComponent } from './mascota.component';
import { MascotaService } from './service/mascota.service';

describe('MascotaComponent', () => {
  let component: MascotaComponent;
  let fixture: ComponentFixture<MascotaComponent>;
  let mascotaService: jasmine.SpyObj<MascotaService>;
  let clienteService: jasmine.SpyObj<ClienteService>;
  let razaService: jasmine.SpyObj<RazaService>;

  beforeEach(async () => {
    mascotaService = jasmine.createSpyObj<MascotaService>('MascotaService', ['listar']);
    mascotaService.listar.and.returnValue(of([]));
    clienteService = jasmine.createSpyObj<ClienteService>('ClienteService', ['listar']);
    clienteService.listar.and.returnValue(of([]));
    razaService = jasmine.createSpyObj<RazaService>('RazaService', ['listar']);
    razaService.listar.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [MascotaComponent],
      providers: [
        { provide: MascotaService, useValue: mascotaService },
        { provide: ClienteService, useValue: clienteService },
        { provide: RazaService, useValue: razaService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MascotaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads pets and the form catalogs when the page opens', () => {
    expect(mascotaService.listar).toHaveBeenCalled();
    expect(clienteService.listar).toHaveBeenCalled();
    expect(razaService.listar).toHaveBeenCalled();
    expect(component.listMascotas).toEqual([]);
    expect(component.cargando).toBeFalse();
    expect(component.errorCarga).toBeFalse();
  });
});