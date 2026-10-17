import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { EspecializacionComponent } from './especializacion.component';
import { EspecializacionService } from './service/especializacion.service';

describe('EspecializacionComponent', () => {
  let component: EspecializacionComponent;
  let fixture: ComponentFixture<EspecializacionComponent>;
  let especializacionService: jasmine.SpyObj<EspecializacionService>;

  beforeEach(async () => {
    especializacionService = jasmine.createSpyObj<EspecializacionService>('EspecializacionService', ['listar']);
    especializacionService.listar.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [EspecializacionComponent],
      providers: [{ provide: EspecializacionService, useValue: especializacionService }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EspecializacionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads specializations when the page opens', () => {
    expect(especializacionService.listar).toHaveBeenCalled();
    expect(component.listEspecializaciones).toEqual([]);
    expect(component.cargando).toBeFalse();
    expect(component.errorCarga).toBeFalse();
  });
});