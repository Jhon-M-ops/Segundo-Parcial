import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Cita, CitaDetalle, CitaFiltro, CitaResumen, CitaRq } from 'src/app/models/cita';
import { MiRespuestaRs, Pagina } from 'src/app/models/respuesta';
import { BackendService } from 'src/app/services/backend.service';
import { environment } from 'src/environments/environment';

/**
 * Servicio de consumo de la API de citas.
 * Usa GET para consultar, POST para crear, PUT para editar y PATCH para
 * los cambios de estado.
 */
@Injectable({
  providedIn: 'root'
})
export class CitaService {
  private api = `cita`;

  constructor(private readonly backendService: BackendService) {}

  /**
   * Consulta una página de citas con filtros y ordenamiento (GET).
   * Los filtros vacíos no se envían.
   *
   * @param filtro paginación, orden y filtros.
   */
  listar(filtro: CitaFiltro): Observable<Pagina<CitaResumen>> {
    let params = new HttpParams()
      .set('page', filtro.page)
      .set('size', filtro.size)
      .set('sortBy', filtro.sortBy)
      .set('sortDir', filtro.sortDir);

    (['texto', 'medicoId', 'estado', 'fechaDesde', 'fechaHasta'] as const).forEach((clave) => {
      const valor = filtro[clave];
      if (valor !== null && valor !== undefined && valor !== '') {
        params = params.set(clave, String(valor));
      }
    });

    return this.backendService.get<Pagina<CitaResumen>>(environment.apiUrlAuth, this.api, 'listar', params);
  }

  /**
   * Consulta el detalle de una cita (GET).
   *
   * @param citaId identificador de la cita.
   */
  consultar(citaId: number): Observable<CitaDetalle> {
    return this.backendService.get<CitaDetalle>(environment.apiUrlAuth, this.api, String(citaId));
  }

  /**
   * Lista las citas de una mascota (combo dependiente Mascota → Cita en fórmulas).
   *
   * @param mascotaId identificador de la mascota.
   */
  listarPorMascota(mascotaId: number): Observable<Cita[]> {
    return this.backendService.get<Cita[]>(
      environment.apiUrlAuth, this.api, 'listar-by-mascota', new HttpParams().set('mascotaId', mascotaId)
    );
  }

  /**
   * Crea una cita (POST).
   *
   * @param cita datos de la cita.
   */
  guardar(cita: CitaRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'guardar', cita);
  }

  /**
   * Modifica una cita según su estado (PUT).
   *
   * @param citaId identificador de la cita.
   * @param cita nuevos datos.
   */
  actualizar(citaId: number, cita: CitaRq): Observable<MiRespuestaRs> {
    return this.backendService.put<MiRespuestaRs>(environment.apiUrlAuth, this.api, String(citaId), cita);
  }

  /**
   * Cambia el estado de una cita: confirmar, atender, cancelar (PATCH).
   *
   * @param citaId identificador de la cita.
   * @param estado estado destino.
   */
  cambiarEstado(citaId: number, estado: string): Observable<MiRespuestaRs> {
    return this.backendService.patch<MiRespuestaRs>(
      environment.apiUrlAuth, this.api, `${citaId}/estado`, { estado }
    );
  }

  /**
   * Reactiva una cita cancelada (PATCH). Operación explícita.
   *
   * @param citaId identificador de la cita.
   */
  reactivar(citaId: number): Observable<MiRespuestaRs> {
    return this.backendService.patch<MiRespuestaRs>(environment.apiUrlAuth, this.api, `${citaId}/reactivar`, {});
  }
}
