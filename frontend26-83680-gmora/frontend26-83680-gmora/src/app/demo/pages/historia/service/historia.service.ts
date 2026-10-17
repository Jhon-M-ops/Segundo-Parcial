import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
    Anotacion, AnotacionFiltro, AnotacionRq, HistoriaDetalle, HistoriaFiltro, HistoriaResumen, HistoriaRq
} from 'src/app/models/historia';
import { MiRespuestaRs, Pagina } from 'src/app/models/respuesta';
import { BackendService } from 'src/app/services/backend.service';
import { environment } from 'src/environments/environment';

/**
 * Servicio de consumo de la API de historias clínicas y anotaciones.
 * GET para consultar, POST para crear y PATCH para cerrar.
 */
@Injectable({
  providedIn: 'root'
})
export class HistoriaService {
  private api = `historia-clinica`;

  constructor(private readonly backendService: BackendService) {}

  /**
   * Consulta una página de historias con filtros y orden (GET).
   *
   * @param filtro paginación, orden y filtros.
   */
  listar(filtro: HistoriaFiltro): Observable<Pagina<HistoriaResumen>> {
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
    return this.backendService.get<Pagina<HistoriaResumen>>(environment.apiUrlAuth, this.api, 'listar', params);
  }

  /**
   * Consulta el detalle de una historia (GET).
   *
   * @param historiaId identificador de la historia.
   */
  consultar(historiaId: number): Observable<HistoriaDetalle> {
    return this.backendService.get<HistoriaDetalle>(environment.apiUrlAuth, this.api, String(historiaId));
  }

  /**
   * Crea una historia clínica (POST).
   *
   * @param historia datos de la historia.
   */
  guardar(historia: HistoriaRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'guardar', historia);
  }

  /**
   * Cierra una historia (PATCH). Una historia cerrada no admite anotaciones.
   *
   * @param historiaId identificador de la historia.
   */
  cerrar(historiaId: number): Observable<MiRespuestaRs> {
    return this.backendService.patch<MiRespuestaRs>(environment.apiUrlAuth, this.api, `${historiaId}/cerrar`, {});
  }

  /**
   * Consulta el histórico de anotaciones de una historia (GET).
   *
   * @param historiaId identificador de la historia.
   * @param filtro paginación, orden por fecha y filtros.
   */
  listarAnotaciones(historiaId: number, filtro: AnotacionFiltro): Observable<Pagina<Anotacion>> {
    let params = new HttpParams()
      .set('page', filtro.page)
      .set('size', filtro.size)
      .set('sortDir', filtro.sortDir);
    (['texto', 'fechaDesde', 'fechaHasta'] as const).forEach((clave) => {
      const valor = filtro[clave];
      if (valor !== null && valor !== undefined && valor !== '') {
        params = params.set(clave, String(valor));
      }
    });
    return this.backendService.get<Pagina<Anotacion>>(
      environment.apiUrlAuth, this.api, `${historiaId}/anotaciones`, params
    );
  }

  /**
   * Consulta el detalle de una anotación (GET).
   *
   * @param historiaId identificador de la historia.
   * @param anotacionId identificador de la anotación.
   */
  consultarAnotacion(historiaId: number, anotacionId: number): Observable<Anotacion> {
    return this.backendService.get<Anotacion>(
      environment.apiUrlAuth, this.api, `${historiaId}/anotaciones/${anotacionId}`
    );
  }

  /**
   * Agrega una anotación a una historia (POST).
   *
   * @param historiaId identificador de la historia.
   * @param anotacion datos de la anotación.
   */
  guardarAnotacion(historiaId: number, anotacion: AnotacionRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(
      environment.apiUrlAuth, this.api, `${historiaId}/anotaciones`, anotacion
    );
  }
}
