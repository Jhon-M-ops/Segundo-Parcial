import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { MedicamentoComponent } from './medicamento.component';
import { MedicamentoService } from './service/medicamento.service';

describe('MedicamentoComponent', () => {
  let component: MedicamentoComponent;
  let fixture: ComponentFixture<MedicamentoComponent>;
  let medicamentoService: jasmine.SpyObj<MedicamentoService>;

  beforeEach(async () => {
    medicamentoService = jasmine.createSpyObj<MedicamentoService>('MedicamentoService', ['listar']);
    medicamentoService.listar.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [MedicamentoComponent],
      providers: [{ provide: MedicamentoService, useValue: medicamentoService }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MedicamentoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads medications when the page opens', () => {
    expect(medicamentoService.listar).toHaveBeenCalled();
    expect(component.listMedicamentos).toEqual([]);
    expect(component.cargando).toBeFalse();
    expect(component.errorCarga).toBeFalse();
  });

  it('marks expired medications', () => {
    const vencido = { nombre: 'Ivermectina', fechaVence: '2020-01-01' };
    const vigente = { nombre: 'Vitaminas', fechaVence: '2999-01-01' };

    expect(component.estaVencido(vencido)).toBeTrue();
    expect(component.estaVencido(vigente)).toBeFalse();
    expect(component.estaVencido({ nombre: 'Sin fecha' })).toBeFalse();
  });
});