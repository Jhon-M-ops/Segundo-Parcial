import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Medico, MedicoRq } from 'src/app/models/medico';
import { MiRespuestaRs } from 'src/app/models/respuesta';
import { BackendService } from 'src/app/services/backend.service';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MedicoService {
  private api = `medico`;

  constructor(private readonly backendService: BackendService) {}

  listar(): Observable<Medico[]> {
    return this.backendService.get<Medico[]>(environment.apiUrlAuth, this.api, 'listar');
  }

  guardar(medico: MedicoRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'guardar', medico);
  }

  actualizar(medico: MedicoRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'actualizar', medico);
  }

  eliminar(id: number): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'eliminar', { id });
  }
}