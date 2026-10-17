import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { RazaComponent } from './raza.component';
import { RazaService } from './service/raza.service';

describe('RazaComponent', () => {
  let component: RazaComponent;
  let fixture: ComponentFixture<RazaComponent>;
  let razaService: jasmine.SpyObj<RazaService>;

  beforeEach(async () => {
    razaService = jasmine.createSpyObj<RazaService>('RazaService', ['listar']);
    razaService.listar.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [RazaComponent],
      providers: [{ provide: RazaService, useValue: razaService }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RazaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads breeds when the page opens', () => {
    expect(razaService.listar).toHaveBeenCalled();
    expect(component.listRazas).toEqual([]);
    expect(component.cargando).toBeFalse();
    expect(component.errorCarga).toBeFalse();
  });

  it('filters the table by species', () => {
    component.listRazas = [
      { razaId: 1, nombre: 'Golden Retriever', especie: 'Canino' },
      { razaId: 2, nombre: 'Siamés', especie: 'Felino' }
    ];
    component.especieFiltro = 'Felino';

    expect(component.razaFiltradas.length).toBe(1);
    expect(component.razaFiltradas[0].nombre).toBe('Siamés');
    expect(component.especies).toEqual(['Canino', 'Felino']);
  });

  it('filters the table ignoring accents', () => {
    component.listRazas = [{ razaId: 2, nombre: 'Siamés', especie: 'Felino' }];
    component.terminoBusqueda = 'siames';

    expect(component.razaFiltradas.length).toBe(1);
  });
});