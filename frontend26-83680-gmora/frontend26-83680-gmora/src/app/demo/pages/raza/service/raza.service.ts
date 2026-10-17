import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Raza, RazaRq } from 'src/app/models/raza';
import { MiRespuestaRs } from 'src/app/models/respuesta';
import { BackendService } from 'src/app/services/backend.service';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RazaService {
  private api = `raza`;

  constructor(private readonly backendService: BackendService) {}

  listar(): Observable<Raza[]> {
    return this.backendService.get<Raza[]>(environment.apiUrlAuth, this.api, 'listar');
  }

  guardar(raza: RazaRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'guardar', raza);
  }

  actualizar(raza: RazaRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'actualizar', raza);
  }

  eliminar(razaId: number): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'eliminar', { razaId });
  }
}