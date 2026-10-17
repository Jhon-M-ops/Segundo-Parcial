import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { EspecializacionService } from '../especializacion/service/especializacion.service';
import { MedicoComponent } from './medico.component';
import { MedicoService } from './service/medico.service';

describe('MedicoComponent', () => {
  let component: MedicoComponent;
  let fixture: ComponentFixture<MedicoComponent>;
  let medicoService: jasmine.SpyObj<MedicoService>;
  let especializacionService: jasmine.SpyObj<EspecializacionService>;

  beforeEach(async () => {
    medicoService = jasmine.createSpyObj<MedicoService>('MedicoService', ['listar']);
    medicoService.listar.and.returnValue(of([]));
    especializacionService = jasmine.createSpyObj<EspecializacionService>('EspecializacionService', ['listar']);
    especializacionService.listar.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [MedicoComponent],
      providers: [
        { provide: MedicoService, useValue: medicoService },
        { provide: EspecializacionService, useValue: especializacionService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MedicoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads doctors and the specialization catalog when the page opens', () => {
    expect(medicoService.listar).toHaveBeenCalled();
    expect(especializacionService.listar).toHaveBeenCalled();
    expect(component.listMedicos).toEqual([]);
    expect(component.cargando).toBeFalse();
    expect(component.errorCarga).toBeFalse();
  });
});