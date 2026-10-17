package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de acceso a datos de las citas.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Lista las citas de una mascota, de la más reciente a la más antigua.
     *
     * @param mascota mascota propietaria de las citas.
     * @return citas de la mascota.
     */
    List<Cita> findAllByMascotaOrderByFechaHoraDesc(Mascota mascota);

    /**
     * Busca citas con filtros opcionales, paginación y ordenamiento.
     * Cada filtro se ignora cuando llega nulo.
     *
     * @param texto    patrón en minúsculas (con %) para mascota, cliente, veterinario o motivo.
     * @param medicoId id del veterinario.
     * @param estado   estado exacto (en mayúsculas).
     * @param desde    fecha-hora mínima (inclusive).
     * @param hasta    fecha-hora máxima (exclusive).
     * @param pageable página, tamaño y orden.
     * @return página de citas.
     */
    @Query("""
            SELECT c FROM Cita c
            WHERE (:medicoId IS NULL OR c.medico.id = :medicoId)
              AND (:estado IS NULL OR c.estado = :estado)
              AND (:desde IS NULL OR c.fechaHora >= :desde)
              AND (:hasta IS NULL OR c.fechaHora < :hasta)
              AND (:texto IS NULL
                   OR LOWER(c.mascota.nombreMascota) LIKE :texto
                   OR LOWER(CONCAT(c.mascota.cliente.nombres, ' ', c.mascota.cliente.apellidos)) LIKE :texto
                   OR LOWER(CONCAT(c.medico.nombres, ' ', c.medico.apellidos)) LIKE :texto
                   OR LOWER(c.motivo) LIKE :texto)
            """)
    Page<Cita> buscar(@Param("texto") String texto,
                      @Param("medicoId") Long medicoId,
                      @Param("estado") String estado,
                      @Param("desde") LocalDateTime desde,
                      @Param("hasta") LocalDateTime hasta,
                      Pageable pageable);

    /**
     * Cuenta las citas NO canceladas de un veterinario dentro de un rango de tiempo.
     * Se usa para validar que no tenga dos citas en el mismo horario (regla 4).
     *
     * @param medicoId  veterinario.
     * @param desde     inicio del rango (inclusive).
     * @param hasta     fin del rango (inclusive).
     * @param excluirId cita que se ignora (la que se está editando); use -1 al crear.
     * @return cantidad de citas que chocan.
     */
    @Query("""
            SELECT COUNT(c) FROM Cita c
            WHERE c.medico.id = :medicoId
              AND c.estado <> 'CANCELADA'
              AND c.citaId <> :excluirId
              AND c.fechaHora BETWEEN :desde AND :hasta
            """)
    long contarCitasDelMedicoEnRango(@Param("medicoId") Long medicoId,
                                     @Param("desde") LocalDateTime desde,
                                     @Param("hasta") LocalDateTime hasta,
                                     @Param("excluirId") Long excluirId);

    /**
     * Cuenta las citas NO canceladas de una mascota dentro de un rango de tiempo.
     * Se usa para validar que una mascota no tenga citas simultáneas (regla 5).
     *
     * @param mascotaId mascota.
     * @param desde     inicio del rango (inclusive).
     * @param hasta     fin del rango (inclusive).
     * @param excluirId cita que se ignora (la que se está editando); use -1 al crear.
     * @return cantidad de citas que chocan.
     */
    @Query("""
            SELECT COUNT(c) FROM Cita c
            WHERE c.mascota.mascotaId = :mascotaId
              AND c.estado <> 'CANCELADA'
              AND c.citaId <> :excluirId
              AND c.fechaHora BETWEEN :desde AND :hasta
            """)
    long contarCitasDeMascotaEnRango(@Param("mascotaId") Integer mascotaId,
                                     @Param("desde") LocalDateTime desde,
                                     @Param("hasta") LocalDateTime hasta,
                                     @Param("excluirId") Long excluirId);

    /**
     * @param mascota mascota a revisar.
     * @return {@code true} si la mascota tiene citas (impide eliminarla).
     */
    boolean existsByMascota(Mascota mascota);

    /**
     * @param medico veterinario a revisar.
     * @return {@code true} si el veterinario tiene citas (impide eliminarlo).
     */
    boolean existsByMedico(Medico medico);
}
