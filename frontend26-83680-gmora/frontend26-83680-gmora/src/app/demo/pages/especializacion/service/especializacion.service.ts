import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Especializacion, EspecializacionRq } from 'src/app/models/especializacion';
import { MiRespuestaRs } from 'src/app/models/respuesta';
import { BackendService } from 'src/app/services/backend.service';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EspecializacionService {
  private api = `especializacion`;

  constructor(private readonly backendService: BackendService) {}

  listar(): Observable<Especializacion[]> {
    return this.backendService.get<Especializacion[]>(environment.apiUrlAuth, this.api, 'listar');
  }

  guardar(especializacion: EspecializacionRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'guardar', especializacion);
  }

  actualizar(especializacion: EspecializacionRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'actualizar', especializacion);
  }

  eliminar(id: number): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'eliminar', { id });
  }
}