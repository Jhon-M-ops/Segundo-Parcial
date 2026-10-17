import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AdminComponent } from './theme/layout/admin/admin.component';
import { ClienteComponent } from './demo/pages/usuario/usuario.component';
import { MascotaComponent } from './demo/pages/mascota/mascota.component';
import { MedicoComponent } from './demo/pages/medico/medico.component';
import { EspecializacionComponent } from './demo/pages/especializacion/especializacion.component';
import { RazaComponent } from './demo/pages/raza/raza.component';
import { MedicamentoComponent } from './demo/pages/medicamento/medicamento.component';
import { CitaComponent } from './demo/pages/cita/cita.component';
import { HistoriaComponent } from './demo/pages/historia/historia.component';
import { FormulaComponent } from './demo/pages/formula/formula.component';


export const routes: Routes = [
  {
    path: '',
    redirectTo: 'inicio',
    pathMatch: 'full'
  },  
  {
    path: 'inicio',
    component: AdminComponent,
    data: { title: 'Inicio' },
    children: [      
      { path: 'clientes', component: ClienteComponent, data: { title: 'Clientes' }},
      { path: 'usuarios', redirectTo: 'clientes', pathMatch: 'full' },
      { path: 'mascotas', component: MascotaComponent, data: { title: 'Mascotas' }},
      { path: 'medicos', component: MedicoComponent, data: { title: 'Médicos' }},
      { path: 'especializaciones', component: EspecializacionComponent, data: { title: 'Especializaciones' }},
      { path: 'razas', component: RazaComponent, data: { title: 'Razas' }},
      { path: 'medicamentos', component: MedicamentoComponent, data: { title: 'Medicamentos' }},
      { path: 'citas', component: CitaComponent, data: { title: 'Citas' }},
      { path: 'historias', component: HistoriaComponent, data: { title: 'Historias clínicas' }},
      { path: 'formulas', component: FormulaComponent, data: { title: 'Fórmulas' }}
      /* Inserte nuevos menus aqui */    
    ]
  },
  { path: '**', redirectTo: 'inicio' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}