import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Mascota, MascotaRq } from 'src/app/models/mascota';
import { MiRespuestaRs } from 'src/app/models/respuesta';
import { BackendService } from 'src/app/services/backend.service';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MascotaService {
  private api = `mascota`;

  constructor(private readonly backendService: BackendService) {}

  listar(): Observable<Mascota[]> {
    return this.backendService.get<Mascota[]>(environment.apiUrlAuth, this.api, 'listar');
  }

  /**
   * Lista únicamente las mascotas que pertenecen a un cliente (combo dependiente).
   *
   * @param clienteId identificador del cliente.
   */
  listarPorCliente(clienteId: number): Observable<Mascota[]> {
    return this.backendService.get<Mascota[]>(
      environment.apiUrlAuth, this.api, 'listar-by-cliente', new HttpParams().set('clienteId', clienteId)
    );
  }

  guardar(mascota: MascotaRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'guardar', mascota);
  }

  actualizar(mascota: MascotaRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'actualizar', mascota);
  }

  eliminar(mascotaId: number): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'eliminar', { mascotaId });
  }
}