import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Medicamento, MedicamentoRq } from 'src/app/models/medicamento';
import { MiRespuestaRs } from 'src/app/models/respuesta';
import { BackendService } from 'src/app/services/backend.service';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MedicamentoService {
  private api = `medicamento`;

  constructor(private readonly backendService: BackendService) {}

  listar(): Observable<Medicamento[]> {
    return this.backendService.get<Medicamento[]>(environment.apiUrlAuth, this.api, 'listar');
  }

  guardar(medicamento: MedicamentoRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'guardar', medicamento);
  }

  actualizar(medicamento: MedicamentoRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'actualizar', medicamento);
  }

  eliminar(id: number): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'eliminar', { id });
  }
}