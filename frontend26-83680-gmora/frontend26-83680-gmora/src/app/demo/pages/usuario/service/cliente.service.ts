import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Cliente, ClienteRq } from 'src/app/models/cliente';
import { MiRespuestaRs } from 'src/app/models/respuesta';
import { BackendService } from 'src/app/services/backend.service';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private api = `cliente`;

  constructor(private readonly backendService: BackendService) {}

  listar(): Observable<Cliente[]> {
    return this.backendService.get<Cliente[]>(environment.apiUrlAuth, this.api, 'listar');
  }

  guardar(cliente: ClienteRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'guardar', cliente);
  }

  actualizar(cliente: ClienteRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'actualizar', cliente);
  }

  eliminar(clienteId: number): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'eliminar', { clienteId });
  }
}