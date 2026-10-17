import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ClienteComponent } from './usuario.component';
import { ClienteService } from './service/cliente.service';

describe('ClienteComponent', () => {
  let component: ClienteComponent;
  let fixture: ComponentFixture<ClienteComponent>;
  let clienteService: jasmine.SpyObj<ClienteService>;

  beforeEach(async () => {
    clienteService = jasmine.createSpyObj<ClienteService>('ClienteService', ['listar']);
    clienteService.listar.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [ClienteComponent],
      providers: [{ provide: ClienteService, useValue: clienteService }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClienteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads clients when the page opens', () => {
    expect(clienteService.listar).toHaveBeenCalled();
    expect(component.listClientes).toEqual([]);
    expect(component.cargando).toBeFalse();
    expect(component.errorCarga).toBeFalse();
  });
});