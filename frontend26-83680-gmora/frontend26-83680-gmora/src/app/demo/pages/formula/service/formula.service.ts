import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { FormulaDetalle, FormulaFiltro, FormulaResumen, FormulaRq } from 'src/app/models/formula';
import { MiRespuestaRs, Pagina } from 'src/app/models/respuesta';
import { BackendService } from 'src/app/services/backend.service';
import { environment } from 'src/environments/environment';

/**
 * Servicio de consumo de la API de fórmulas médicas.
 * Usa los métodos HTTP según la operación: GET, POST, PUT y PATCH.
 */
@Injectable({
  providedIn: 'root'
})
export class FormulaService {
  private api = `formula`;

  constructor(private readonly backendService: BackendService) {}

  /**
   * Consulta una página de fórmulas con filtros y ordenamiento (GET).
   * Los filtros vacíos no se envían al backend.
   *
   * @param filtro paginación, orden y filtros.
   */
  listar(filtro: FormulaFiltro): Observable<Pagina<FormulaResumen>> {
    let params = new HttpParams()
      .set('page', filtro.page)
      .set('size', filtro.size)
      .set('sortBy', filtro.sortBy)
      .set('sortDir', filtro.sortDir);

    (['texto', 'medicoId', 'medicamentoId', 'activo', 'fechaDesde', 'fechaHasta'] as const).forEach((clave) => {
      const valor = filtro[clave];
      if (valor !== null && valor !== undefined && valor !== '') {
        params = params.set(clave, String(valor));
      }
    });

    return this.backendService.get<Pagina<FormulaResumen>>(environment.apiUrlAuth, this.api, 'listar', params);
  }

  /**
   * Consulta el detalle completo de una fórmula (GET).
   *
   * @param formulaId identificador de la fórmula.
   */
  consultar(formulaId: number): Observable<FormulaDetalle> {
    return this.backendService.get<FormulaDetalle>(environment.apiUrlAuth, this.api, String(formulaId));
  }

  /**
   * Crea una fórmula (POST).
   *
   * @param formula datos de la fórmula.
   */
  guardar(formula: FormulaRq): Observable<MiRespuestaRs> {
    return this.backendService.post<MiRespuestaRs>(environment.apiUrlAuth, this.api, 'guardar', formula);
  }

  /**
   * Modifica una fórmula (PUT).
   *
   * @param formulaId identificador de la fórmula.
   * @param formula nuevos datos.
   */
  actualizar(formulaId: number, formula: FormulaRq): Observable<MiRespuestaRs> {
    return this.backendService.put<MiRespuestaRs>(environment.apiUrlAuth, this.api, String(formulaId), formula);
  }

  /**
   * Inactiva una fórmula (PATCH). Reemplaza la eliminación física.
   *
   * @param formulaId identificador de la fórmula.
   */
  inactivar(formulaId: number): Observable<MiRespuestaRs> {
    return this.backendService.patch<MiRespuestaRs>(environment.apiUrlAuth, this.api, `${formulaId}/inactivar`, {});
  }
}
