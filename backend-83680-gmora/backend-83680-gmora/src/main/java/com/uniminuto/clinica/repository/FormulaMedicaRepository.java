package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.HistoriaClinica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Repositorio de acceso a datos de las fórmulas médicas.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Busca fórmulas aplicando filtros opcionales, paginación y ordenamiento.
     * Cada filtro se ignora cuando llega nulo.
     *
     * @param texto         patrón en minúsculas (con comodines %) para buscar por
     *                      mascota, cliente, veterinario o medicamento.
     * @param medicoId      id del veterinario.
     * @param medicamentoId id del medicamento.
     * @param activo        estado de la fórmula.
     * @param fechaDesde    fecha de emisión mínima (inclusive).
     * @param fechaHasta    fecha de emisión máxima (inclusive).
     * @param pageable      página, tamaño y orden solicitados.
     * @return página de fórmulas que cumplen los filtros.
     */
    @Query("""
            SELECT f FROM FormulaMedica f
            WHERE (:medicoId IS NULL OR f.medico.id = :medicoId)
              AND (:medicamentoId IS NULL OR f.medicamento.id = :medicamentoId)
              AND (:activo IS NULL OR f.activo = :activo)
              AND (:fechaDesde IS NULL OR f.fechaEmision >= :fechaDesde)
              AND (:fechaHasta IS NULL OR f.fechaEmision <= :fechaHasta)
              AND (:texto IS NULL
                   OR LOWER(f.mascota.nombreMascota) LIKE :texto
                   OR LOWER(CONCAT(f.mascota.cliente.nombres, ' ', f.mascota.cliente.apellidos)) LIKE :texto
                   OR LOWER(CONCAT(f.medico.nombres, ' ', f.medico.apellidos)) LIKE :texto
                   OR LOWER(f.medicamento.nombre) LIKE :texto)
            """)
    Page<FormulaMedica> buscar(@Param("texto") String texto,
                               @Param("medicoId") Long medicoId,
                               @Param("medicamentoId") Long medicamentoId,
                               @Param("activo") Boolean activo,
                               @Param("fechaDesde") LocalDate fechaDesde,
                               @Param("fechaHasta") LocalDate fechaHasta,
                               Pageable pageable);

    /**
     * Indica si ya existe una fórmula activa del mismo medicamento para la misma consulta
     * (usado al crear, para evitar duplicidad).
     *
     * @param cita        consulta asociada.
     * @param medicamento medicamento formulado.
     * @return {@code true} si ya existe.
     */
    boolean existsByCitaAndMedicamentoAndActivoTrue(Cita cita, Medicamento medicamento);

    /**
     * Igual que {@link #existsByCitaAndMedicamentoAndActivoTrue} pero ignorando una fórmula
     * (usado al editar, para no compararla consigo misma).
     *
     * @param cita        consulta asociada.
     * @param medicamento medicamento formulado.
     * @param formulaId   fórmula que se ignora.
     * @return {@code true} si existe otra fórmula igual.
     */
    boolean existsByCitaAndMedicamentoAndActivoTrueAndFormulaIdNot(Cita cita, Medicamento medicamento, Long formulaId);

    /**
     * @param mascota mascota a revisar.
     * @return {@code true} si la mascota tiene fórmulas (impide eliminarla).
     */
    boolean existsByMascota(Mascota mascota);

    /**
     * @param medico veterinario a revisar.
     * @return {@code true} si el veterinario emitió fórmulas (impide eliminarlo).
     */
    boolean existsByMedico(Medico medico);

    /**
     * @param medicamento medicamento a revisar.
     * @return {@code true} si el medicamento está en alguna fórmula (impide eliminarlo).
     */
    boolean existsByMedicamento(Medicamento medicamento);

    /**
     * Enlaza con una historia clínica las fórmulas de una cita que aún no tenían historia.
     * Debe llamarse (dentro de una transacción) cuando se crea la historia de esa cita,
     * para que no queden fórmulas sin su historia (relación Historia 1:N Fórmula).
     *
     * @param cita     cita de origen.
     * @param historia historia recién creada.
     * @return cantidad de fórmulas enlazadas.
     */
    @Modifying
    @Query("UPDATE FormulaMedica f SET f.historiaClinica = :historia "
            + "WHERE f.cita = :cita AND f.historiaClinica IS NULL")
    int vincularConHistoria(@Param("cita") Cita cita, @Param("historia") HistoriaClinica historia);
}
